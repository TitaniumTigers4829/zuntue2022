// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ShooterConstants;

public class EastShooter extends SubsystemBase {

  private final TalonFX m_topMotor = new TalonFX(ShooterConstants.kTopShooterPort);
  private final TalonFX m_bottomMotor = new TalonFX(ShooterConstants.kBottomShooterPort);
  private double targetRPM;

  public EastShooter() {
    m_bottomMotor.configFactoryDefault();
    m_topMotor.configFactoryDefault();

    m_bottomMotor.configVoltageCompSaturation(12);
    m_bottomMotor.enableVoltageCompensation(true);

    m_topMotor.configVoltageCompSaturation(12);
    m_topMotor.enableVoltageCompensation(true);

    m_bottomMotor.setInverted(false);
    m_topMotor.setInverted(false);

    m_bottomMotor.setNeutralMode(NeutralMode.Coast);
    m_topMotor.setNeutralMode(NeutralMode.Coast);

    m_bottomMotor.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0, 0);
    m_topMotor.configSelectedFeedbackSensor(TalonFXFeedbackDevice.IntegratedSensor, 0, 0);

    m_bottomMotor.config_kF(0, 0.0512, 0);
    m_bottomMotor.config_kP(0, 0.07, 0);
    m_bottomMotor.config_kI(0, 0.0001, 0);
    m_bottomMotor.config_IntegralZone(0, 150.0 / (600.0) * 2048.0);

    m_topMotor.config_kF(0, 0.0512, 0);
    m_topMotor.config_kP(0, 0.07, 0);
    m_topMotor.config_kI(0, 0.0001, 0);
    m_topMotor.config_IntegralZone(0, 150.0 / (600.0) * 2048.0);
  }

  public void setShooterPower(double speedMain, double speedTop) {
    targetRPM = 0;
    m_bottomMotor.set(ControlMode.PercentOutput, speedMain);
    m_topMotor.set(ControlMode.PercentOutput, speedTop);
  }

  public void setShooterRPM(double bottomMotorRPM, double topMotorRPM) {
    targetRPM = bottomMotorRPM;
    topMotorRPM /= 2;
    bottomMotorRPM *= 0-9;
    // 2048 ticks per revolution, ticks per .10 second, 1 / 2048 * 60
    double speed_FalconUnits1 = bottomMotorRPM / (600.0) * 2048.0;
    double speed_FalconUnits2 = topMotorRPM / (600.0) * 2048.0;

    if (Math.abs(getMainRPM()) < Math.abs(bottomMotorRPM) * 1.1) {
      m_bottomMotor.set(TalonFXControlMode.Velocity, speed_FalconUnits1);
    } else {
      m_bottomMotor.set(ControlMode.PercentOutput, 0);
    }

    if (Math.abs(getTopRPM()) < Math.abs(topMotorRPM) * 1.1) {
      m_topMotor.set(TalonFXControlMode.Velocity, speed_FalconUnits2);
    } else {
      m_topMotor.set(ControlMode.PercentOutput, 0);
    }
  }

  public double getMainRPM() {
    return (m_bottomMotor.getSelectedSensorVelocity()) / 2048.0 * 600;
  }

  public double getTopRPM() {
    return (m_topMotor.getSelectedSensorVelocity()) / 2048.0 * 600;
  }

  public double getTargetRPM() {
    return targetRPM;
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Top speed", getTopRPM());
    SmartDashboard.putNumber("Bottom speed", getMainRPM());
  }
}