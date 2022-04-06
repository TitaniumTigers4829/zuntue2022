// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ShooterConstants;

public class ShooterSubsystem extends SubsystemBase {

  private final TalonFX m_topMotor = new TalonFX(ShooterConstants.kTopShooterMotorPort);
  private final TalonFX m_bottomMotor = new TalonFX(ShooterConstants.kBottomShooterMotorPort);
  private double topMotorTargetRPM;
  private double bottomMotorTargetRPM;
  private double topOutput;
  private double botOutput;


  private PIDController topPID = new PIDController(ShooterConstants.topkP, 0, 0);
  private SimpleMotorFeedforward topFF = new SimpleMotorFeedforward(ShooterConstants.topkS, ShooterConstants.topkV);

  private PIDController bottomPID = new PIDController(ShooterConstants.bottomkP, 0, 0);
  private SimpleMotorFeedforward bottomFF = new SimpleMotorFeedforward(ShooterConstants.bottomkS, ShooterConstants.bottomkV);


  public ShooterSubsystem() {
    m_bottomMotor.configFactoryDefault();
    m_topMotor.configFactoryDefault();

    m_bottomMotor.configVoltageCompSaturation(12);
    m_bottomMotor.enableVoltageCompensation(true);

    m_topMotor.configVoltageCompSaturation(12);
    m_topMotor.enableVoltageCompensation(true);

    m_bottomMotor.setInverted(true);
    m_topMotor.setInverted(true);

    m_bottomMotor.setNeutralMode(NeutralMode.Coast);
    m_topMotor.setNeutralMode(NeutralMode.Coast);

    m_bottomMotor.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0, 0);
    m_topMotor.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0, 0);

//    m_bottomMotor.config_kF(0, 0.0512, 0);
//    m_bottomMotor.config_kP(0, 0.07, 0);
//    m_bottomMotor.config_kI(0, 0.0001, 0);
//    m_bottomMotor.config_IntegralZone(0, 150.0 / (600.0) * 2048.0);
//
//    m_topMotor.config_kF(0, 0.0512, 0);
//    m_topMotor.config_kP(0, 0.07, 0);
//    m_topMotor.config_kI(0, 0.0001, 0);
//    m_topMotor.config_IntegralZone(0, 150.0 / (600.0) * 2048.0);

    m_bottomMotor.setStatusFramePeriod(StatusFrame.Status_1_General, 250);
    m_topMotor.setStatusFramePeriod(StatusFrame.Status_1_General, 250);
  }

  public void setShooterRPM(double bottomMotorRPM, double topMotorRPM) {
    topMotorTargetRPM = topMotorRPM;
    bottomMotorTargetRPM = bottomMotorRPM;
    // 2048 ticks per revolution, ticks per .10 second, 1 / 2048 * 60
    double speed_FalconUnits1 = bottomMotorRPM / (600.0) * 2048.0;
    double speed_FalconUnits2 = topMotorRPM / (600.0) * 2048.0;

    if (Math.abs(getBottomRPM()) < Math.abs(bottomMotorRPM) * 1.1) {
      m_bottomMotor.set(TalonFXControlMode.Velocity, speed_FalconUnits1);
    } else {
      m_bottomMotor.set(ControlMode.PercentOutput, 0);
    }

    if (Math.abs(getTopRPM()) < Math.abs(topMotorRPM) * 1.1) {
      m_topMotor.set(TalonFXControlMode.Velocity, speed_FalconUnits2);
    } else {
      m_topMotor.set(ControlMode.PercentOutput, 0);
    }

    SmartDashboard.putNumber("Top target RPM", topMotorTargetRPM);
    SmartDashboard.putNumber("Bottom target RPM", bottomMotorTargetRPM);
  }

  public void setShooterRPMImproved(double bottomMotorRPM, double topMotorRPM) {
    topMotorTargetRPM = topMotorRPM;
    bottomMotorTargetRPM = bottomMotorRPM;

    SmartDashboard.putNumber("i BOT target RPM", getBottomRPM());
//    SmartDashboard.putNumber("i Bot target RPM", bottomMotorTargetRPM);
    SmartDashboard.putNumber("i BOT error", bottomMotorTargetRPM - getBottomRPM());

    SmartDashboard.putNumber("i TOP target RPM", getTopRPM());
    SmartDashboard.putNumber("i TOP error", topMotorTargetRPM - getTopRPM());


    topOutput =
        topPID.calculate(getTopRPM(), topMotorTargetRPM) +
             topFF.calculate(topMotorTargetRPM);

    botOutput =
        bottomPID.calculate(getBottomRPM(), bottomMotorTargetRPM) +
            bottomFF.calculate(bottomMotorTargetRPM);

    m_topMotor.set(ControlMode.PercentOutput, topOutput/12);
    m_bottomMotor.set(ControlMode.PercentOutput, botOutput/12);
  }


  public boolean isShooterWithinAcceptableError() {
    return Math.abs(topMotorTargetRPM-getTopRPM()) < 75 * 2 && Math.abs(bottomMotorTargetRPM-getBottomRPM()) < 50 * 2;
//    return (Math.abs(m_bottomMotor.getClosedLoopError()) + Math.abs(m_topMotor.getClosedLoopError()));
  }


  public double getTopRPM() {
    return ((m_topMotor.getSelectedSensorVelocity()) / 2048.0) * 600;
  }

  public double getBottomRPM() {
    return (m_bottomMotor.getSelectedSensorVelocity()) / 2048.0 * 600;
  }

  public void setShooterToNeutral() {
    m_topMotor.set(ControlMode.PercentOutput, 0);
    m_bottomMotor.set(ControlMode.PercentOutput, 0);
  }

  @Override
  public void periodic() {
//    SmartDashboard.putNumber("Top RPM", getTopRPM());
//    SmartDashboard.putNumber("Bottom RPM", getBottomRPM());
//    SmartDashboard.putNumber("topMotor output", topOutput/12);

  }
}
