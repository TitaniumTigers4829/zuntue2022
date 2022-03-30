// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.commands.intake.IntakeWithTower;
import frc.robot.commands.shooter.Shoot;
import frc.robot.commands.testing.ClimbManualIndependentControl;
import frc.robot.commands.testing.ClimbManualPairedPIDControl;
import frc.robot.subsystems.ClimbSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.EastShooter;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.LimelightSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.TowerSubsystem;
import java.util.List;
import java.util.function.DoubleSupplier;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */

public class RobotContainer {

  // The robot's subsystems
  private final DriveSubsystem m_robotDrive = new DriveSubsystem();
  private final IntakeSubsystem m_intakeSubsystem = new IntakeSubsystem();
  private final ShooterSubsystem m_shooterSubsystem = new ShooterSubsystem();
  private final ClimbSubsystem m_climbSubsystem = new ClimbSubsystem();
  private final LimelightSubsystem m_Limelight = LimelightSubsystem.getInstance();
  private final TowerSubsystem m_tower = new TowerSubsystem();
  private final EastShooter shooter = new EastShooter();
//  private final LEDsSubsystem m_LEDs = new LEDsSubsystem();

  // The driver's controller
  private final Joystick m_driverController = new Joystick(OIConstants.kDriverControllerPort);
  private final Joystick m_buttonController = new Joystick(OIConstants.kButtonControllerPort);

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
//  type a response if u see this

  public RobotContainer() {
    // Turn off the limelight lights because they are very bright
//    m_Limelight.turnOffLED();
    m_Limelight.turnOnLED();
    // m_LEDs.setLEDsRaw(-0.39); // will normally be handled by commands, just for testing.

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    // Configure the button bindings/joysticks
    configureButtonBindings();
  }

  private static double deadband(double value, double deadband) {
    if (Math.abs(value) > deadband) {
      if (value > 0.0) {
        return (value - deadband) / (1.0 - deadband);
      } else {
        return (value + deadband) / (1.0 - deadband);
      }
    } else {
      return 0.0;
    }
  }

  private static double modifyAxis(DoubleSupplier supplierValue) {
    double value = supplierValue.getAsDouble();

    // Deadband
    value = deadband(value, OIConstants.kDriverControllerDeadband);

    // Square the axis
    value = Math.copySign(value * value, value);

    return value;
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling passing it to a
   * {@link JoystickButton}.
   */
  private void configureButtonBindings() {
//    TODO: figure out what will be mechanically done by competition and get that done in programming
    DoubleSupplier LEFT_STICK_X = () -> m_driverController.getRawAxis(0);
    DoubleSupplier LEFT_STICK_Y = () -> m_driverController.getRawAxis(1);
    DoubleSupplier RIGHT_STICK_X = () -> m_driverController.getRawAxis(2);
    DoubleSupplier RIGHT_STICK_Y = () -> m_driverController.getRawAxis(3);

    JoystickButton X_BUTTON = new JoystickButton(m_driverController, 1);
    JoystickButton A_BUTTON = new JoystickButton(m_driverController, 2);
    JoystickButton B_BUTTON = new JoystickButton(m_driverController, 3);
    JoystickButton Y_BUTTON = new JoystickButton(m_driverController, 4);
    JoystickButton LEFT_BUMPER = new JoystickButton(m_driverController, 5);
    JoystickButton RIGHT_BUMPER = new JoystickButton(m_driverController, 6);
    JoystickButton LEFT_TRIGGER = new JoystickButton(m_driverController, 7);
    JoystickButton RIGHT_TRIGGER = new JoystickButton(m_driverController, 8);
    POVButton UP_DIRECTION_PAD = new POVButton(m_driverController, 0);
    POVButton RIGHT_DIRECTION_PAD = new POVButton(m_driverController, 90);
    POVButton LEFT_DIRECTION_PAD = new POVButton(m_driverController, 270);
    POVButton DOWN_DIRECTION_PAD = new POVButton(m_driverController, 180);
    JoystickButton LEFT_STICK_DEPRESSED = new JoystickButton(m_driverController, 11);

    /**
     * Sets the default command and joystick bindings for the drive train.
     * NOTE: The left stick controls translation of the robot. Turning is controlled by the X axis of the right stick.
     */

    m_robotDrive.setDefaultCommand(
        new RunCommand(
            () ->
                m_robotDrive.drive(
                    modifyAxis(LEFT_STICK_Y) * -1 // xAxis
                        * DriveConstants.kMaxSpeedMetersPerSecond,
                    modifyAxis(LEFT_STICK_X) * -1 // yAxis
                        * DriveConstants.kMaxSpeedMetersPerSecond,
                    modifyAxis(RIGHT_STICK_X) * -1 // rot CCW positive
                        * DriveConstants.kMaxRotationalSpeed,
                    !RIGHT_BUMPER.get()),
            m_robotDrive));
    RIGHT_DIRECTION_PAD.whenPressed(new InstantCommand(m_robotDrive::zeroHeading));

//    new POVButton(m_buttonController, 270).whenPressed(new MidBarLatchHooks(m_climbSubsystem));
//    new POVButton(m_buttonController, 180).whenPressed(new ClimbBottomPosition(m_climbSubsystem));
//    new POVButton(m_buttonController, 90).whenPressed(new MidBarClimb(m_climbSubsystem));

//    RIGHT_BUMPER.toggleWhenPressed(new AcuatorTesting(m_shooterSubsystem, RIGHT_STICK_Y));
//    RIGHT_BUMPER.whenPressed(new InstantCommand(m_shooterSubsystem::setFenderShotHeight));
//    LEFT_TRIGGER.whenPressed(new InstantCommand(m_shooterSubsystem::decreasePos));
//    RIGHT_TRIGGER.whenPressed(new InstantCommand(m_shooterSubsystem::increasePos));

//    // Manual Climb
//    LEFT_DIRECTION_PAD.whenPressed(new InstantCommand(m_climbSubsystem::resetEncoders));
//    JoystickButton LClimbUp = new JoystickButton(m_buttonController, 3);
//    JoystickButton RClimbUp = new JoystickButton(m_buttonController, 4);
//    JoystickButton LClimbDown = new JoystickButton(m_buttonController, 1);
//    JoystickButton RClimbDown = new JoystickButton(m_buttonController, 2);
//    JoystickButton PneumaticsVertical = new JoystickButton(m_buttonController, 7);
//    JoystickButton PneumaticsDown = new JoystickButton(m_buttonController, 6);

//    new JoystickButton(m_buttonController, 9).toggleWhenPressed(
//        new ClimbWithButtons(m_climbSubsystem,
//            LClimbUp::get, LClimbDown::get, RClimbUp::get,
//            RClimbDown::get, PneumaticsVertical::get, PneumaticsDown::get)); // This works

    // Manual control for getting shoot values
    RIGHT_TRIGGER.whenPressed(
        new InstantCommand(shooter::increaseTopRPM));
    LEFT_TRIGGER.whenPressed(
        new InstantCommand(shooter::decreaseTopRPM));
    RIGHT_BUMPER.whenPressed(
        new InstantCommand(shooter::increaseBottomRPM));
    LEFT_BUMPER.whenPressed(
        new InstantCommand(shooter::decreaseBottomRPM));

    // Fender Shot
//    new JoystickButton(m_buttonController, 5).whileHeld(
//        new FenderShot(m_tower, m_shooterSubsystem, false));
//    // Low Shot
//    new JoystickButton(m_buttonController, 8).whileHeld(
//        new LowShot(m_tower, m_shooterSubsystem));
//

    // While held for intake
    LEFT_BUMPER.whileHeld(new IntakeWithTower(m_intakeSubsystem, m_tower));

//    A_BUTTON.whileHeld(new TestShot(m_tower, new EastShooter()));
    A_BUTTON.whileHeld(new Shoot(shooter, m_tower, m_Limelight));

//     Toggle for climb solenoids
////     Intake down
//    new JoystickButton(m_buttonController, 7).whenPressed(
//        new InstantCommand(m_intakeSubsystem::setSolenoidDeployed));
//    // Intake up
//    new JoystickButton(m_buttonController, 6).whenPressed(
//        new InstantCommand(m_intakeSubsystem::setSolenoidRetracted));

    //X_BUTTON.whenPressed(new InstantCommand(m_climbSubsystem::setClimbAngled));
//Y_BUTTON.whenPressed(new InstantCommand(m_climbSubsystem::setClimbVertical));
  }

  private void configureButtonBindingsTest() {
    DoubleSupplier LEFT_STICK_X = () -> m_driverController.getRawAxis(0);
    DoubleSupplier LEFT_STICK_Y = () -> m_driverController.getRawAxis(1);
    DoubleSupplier RIGHT_STICK_X = () -> m_driverController.getRawAxis(2);
    DoubleSupplier RIGHT_STICK_Y = () -> m_driverController.getRawAxis(3);

    JoystickButton X_BUTTON = new JoystickButton(m_driverController, 1);
    JoystickButton A_BUTTON = new JoystickButton(m_driverController, 2);
    JoystickButton B_BUTTON = new JoystickButton(m_driverController, 3);
    JoystickButton Y_BUTTON = new JoystickButton(m_driverController, 4);
    JoystickButton LEFT_BUMPER = new JoystickButton(m_driverController, 5);
    JoystickButton RIGHT_BUMPER = new JoystickButton(m_driverController, 6);
    JoystickButton LEFT_TRIGGER = new JoystickButton(m_driverController, 7);
    JoystickButton RIGHT_TRIGGER = new JoystickButton(m_driverController, 8);
    POVButton UP_DIRECTION_PAD = new POVButton(m_driverController, 0);
    POVButton RIGHT_DIRECTION_PAD = new POVButton(m_driverController, 90);
    POVButton LEFT_DIRECTION_PAD = new POVButton(m_driverController, 270);
    POVButton DOWN_DIRECTION_PAD = new POVButton(m_driverController, 180);
    JoystickButton LEFT_STICK_DEPRESSED = new JoystickButton(m_driverController, 11);

    // Drive/Limelight Testing code
    RIGHT_BUMPER.toggleWhenPressed(new RunCommand(
        () ->
            m_robotDrive.drive(
                modifyAxis(LEFT_STICK_Y) * -1 // xAxis
                    * DriveConstants.kMaxSpeedMetersPerSecond,
                modifyAxis(LEFT_STICK_X) * -1 // yAxis
                    * DriveConstants.kMaxSpeedMetersPerSecond,
                modifyAxis(RIGHT_STICK_X) * -1 // rot CCW positive
                    * DriveConstants.kMaxRotationalSpeed,
                true),
        m_robotDrive));

    LEFT_BUMPER.toggleWhenPressed(new RunCommand(
        () ->
            m_robotDrive.drive(
                modifyAxis(LEFT_STICK_Y) * -1 // xAxis
                    * DriveConstants.kMaxSpeedMetersPerSecond,
                modifyAxis(LEFT_STICK_X) * -1 // yAxis
                    * DriveConstants.kMaxSpeedMetersPerSecond,
                modifyAxis(RIGHT_STICK_X) * -1 // rot CCW positive
                    * DriveConstants.kMaxRotationalSpeed,
                false),
        m_robotDrive));

    RIGHT_DIRECTION_PAD.whenPressed(new InstantCommand(m_robotDrive::zeroHeading));
    LEFT_STICK_DEPRESSED.whenPressed(new InstantCommand(m_Limelight::turnOffLED));

    // Intake command
//    LEFT_TRIGGER.whileHeld(new IntakeActiveTeleop(m_intakeSubsystem));

//    LEFT_TRIGGER.whileHeld(new IntakeWithTower(m_intakeSubsystem, m_tower));
//    RIGHT_TRIGGER.whileHeld(new FenderShot(m_tower, m_shooterSubsystem));
    // Shooter/Tower Testing code
//    UP_DIRECTION_PAD.whenPressed(new InstantCommand(m_tower::setTowerThirdPower));
//    DOWN_DIRECTION_PAD.whenPressed(new InstantCommand(m_shooterSubsystem::setShooterFullSpeed));
//    Y_BUTTON.whenPressed(new InstantCommand(m_tower::setTowerOff));
//    A_BUTTON.whenPressed(new InstantCommand(m_shooterSubsystem::stopShooter));

    // Climb Testing code
    // TODO: Test/tune climb using commands like this
    LEFT_DIRECTION_PAD.whenPressed(new InstantCommand(m_climbSubsystem::resetEncoders));
    A_BUTTON.toggleWhenPressed(
        new ClimbManualIndependentControl(m_climbSubsystem, LEFT_STICK_Y, RIGHT_STICK_Y));
    B_BUTTON.toggleWhenPressed(new ClimbManualPairedPIDControl(m_climbSubsystem, RIGHT_STICK_Y));
    X_BUTTON.whenPressed(new InstantCommand(m_climbSubsystem::setClimbAngled));
    Y_BUTTON.whenPressed(new InstantCommand(m_climbSubsystem::setClimbVertical));

    // Make sure hooks are latched when testing this part
//    RIGHT_BUMPER.whenPressed(new InstantCommand(m_climbSubsystem::setRightHookToBottomPos));
//    LEFT_BUMPER.whenPressed(new InstantCommand(m_climbSubsystem::setLeftHookToBottomPos));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // Create config for trajectory
    TrajectoryConfig config =
        new TrajectoryConfig(
            AutoConstants.kMaxSpeedMetersPerSecond,
            AutoConstants.kMaxAccelerationMetersPerSecondSquared)
            // Add kinematics to ensure max speed is actually obeyed
            .setKinematics(DriveConstants.kDriveKinematics);

    // An example trajectory to follow.  All units in meters.
    Trajectory exampleTrajectory =
        TrajectoryGenerator.generateTrajectory(
            // Start at the origin facing the +X direction
            new Pose2d(0, 0, new Rotation2d(0)),
            // Pass through these two interior waypoints, making an 's' curve path
            List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
            // End 3 meters straight ahead of where we started, facing forward
            new Pose2d(3, 0, new Rotation2d(0)),
            config);

    var thetaController =
        new ProfiledPIDController(
            AutoConstants.kPThetaController, 0, 0, AutoConstants.kThetaControllerConstraints);
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    SwerveControllerCommand swerveControllerCommand =
        new SwerveControllerCommand(
            exampleTrajectory,
            m_robotDrive::getPose, // Functional interface to feed supplier
            DriveConstants.kDriveKinematics,

            // Position controllers
            new PIDController(AutoConstants.kPXController, 0, 0),
            new PIDController(AutoConstants.kPYController, 0, 0),
            thetaController,
            m_robotDrive::setModuleStates,
            m_robotDrive);

    // Reset odometry to the starting pose of the trajectory.
    m_robotDrive.resetOdometry(exampleTrajectory.getInitialPose());

    // Run path following command, then stop at the end.
    return swerveControllerCommand.andThen(() -> m_robotDrive.drive(0, 0, 0, false));
//    return new AutonomousCommand(m_shooterSubsystem, m_tower, m_robotDrive);
//    return new FenderShot(m_tower, m_shooterSubsystem).withTimeout(5)
//        .andThen(
//            new RunCommand(() -> m_robotDrive.drive(-0.2, 0, 0, false))
//                .withTimeout(1)
//        ).andThen(
//            new RunCommand(() -> m_robotDrive.drive(0, 0, 0, false))
//                .withTimeout(1)
//        );
  }
}
