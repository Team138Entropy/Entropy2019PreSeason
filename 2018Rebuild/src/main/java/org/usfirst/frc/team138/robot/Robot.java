package org.usfirst.frc.team138.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.command.Scheduler;
//import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team138.robot.subsystems.*;
import org.usfirst.frc.team138.robot.commands.*;

//import edu.wpi.first.wpilibj.Preferences;

/**
 * This is Master branch.
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory .
 */
public class Robot extends IterativeRobot {
	// Interface with players
    SendableChooser<String> teamChooser;
    SendableChooser<String> startPosChooser;
    SendableChooser<String> autoModeChooser;
    SendableChooser<String> debugModeChooser;
        
    // Subsystems
    public static final Compressor compressor = new Compressor();
    public static final Drivetrain drivetrain = new Drivetrain();
    public static final Grasper grasper = new Grasper();
    public static final Elevator elevator = new Elevator();
    public static final Climber climber = new Climber();
    public static double accumulatedHeading = 0.0; // Accumulate heading angle (target)

    public static final OI oi = new OI();
	
    Preferences prefs = Preferences.getInstance();
    
    // Location lookup
    public static AutoLocations autoLocations;
	
    // Commands
    AutonomousCommand autonomousCommand;
    
    // Global constants
    public static String mode; // "auto" or "teleop"
    public static String gameData;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	drivetrain.DriveTrainInit();
    	compressor.start();
		Sensors.initialize();
		grasper.initialize();
		elevator.ElevatorInit();

		// Smart Dashboard Initialization
		Sensors.updateSmartDashboard();
		SmartDashboard.putData(Scheduler.getInstance());
		
		teamChooser = new SendableChooser<String>();
		teamChooser.addDefault("Red Alliance", "red");
		teamChooser.addObject("Blue Alliance", "blue");
		SmartDashboard.putData("Team:", teamChooser);
		
		startPosChooser = new SendableChooser<String>();
		startPosChooser.addObject("Left", "left");
		startPosChooser.addDefault("Middle", "middle");
		startPosChooser.addObject("Right", "right");
		SmartDashboard.putData("Starting Position:", startPosChooser);
		
		autoModeChooser = new SendableChooser<String>();
		autoModeChooser.addObject("Nothing", "DNE");
		autoModeChooser.addDefault("Automatic", "auto");
		autoModeChooser.addObject("Manual", "manual");
		autoModeChooser.addObject("Test" , "test");
		SmartDashboard.putData("Auto Mode:", autoModeChooser);
					
		debugModeChooser = new SendableChooser<String>();
		debugModeChooser.addObject("Debug", "debug");
		debugModeChooser.addObject("Competition", "competition");
		SmartDashboard.putData("Debug Mode:", debugModeChooser);
		
		SmartDashboard.putBoolean("practiceBot", isPracticeRobot());		
        Robot.accumulatedHeading = 0;
        Constants.AutoEnable=true;

        Constants.practiceBot = isPracticeRobot();
    }
	
	/**
     * This function is called once each time the robot enters Disabled mode.
     * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
     */
    public void disabledInit(){

    }
	
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}
	
	private double getWheelAngle() {
		double wheelAngle = (AutoDrive.rightDistance() - AutoDrive.leftDistance()) / Constants.driveWheelSpacing;
		return wheelAngle * (180 / Math.PI);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString code to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the chooser code above (like the commented example)
	 * or additional comparisons to the switch structure below with additional strings & commands.
	 */
    public void autonomousInit() {
    	mode = "auto";
		SmartDashboard.putData("Team:", teamChooser);
		SmartDashboard.putData("Starting Position:", startPosChooser);		
		SmartDashboard.putData("Auto Mode:", autoModeChooser);
		
		if (Constants.practiceBot) {

			Constants.kPRotate=prefs.getDouble("Rotate KP", Constants.kPRotate);
			Constants.kDRotate=prefs.getDouble("Rotate KD", Constants.kDRotate);
			Constants.kIRotate=prefs.getDouble("Rotate KI", Constants.kIRotate);

			Constants.AutoDriveSpeed=prefs.getDouble("Auto Speed", Constants.AutoDriveSpeed);
			Constants.AutoDriveRotateRate = prefs.getDouble("Auto Rotate", Constants.AutoDriveRotateRate);


			Constants.kPDrive=prefs.getDouble("Drive KP", Constants.kPDrive);
			Constants.kDDrive=prefs.getDouble("Drive KD", Constants.kDDrive);
			Constants.kIDrive=prefs.getDouble("Drive KI", Constants.kIDrive);
		}
    	
    	gameData = DriverStation.getInstance().getGameSpecificMessage();
    	   	
    	autoLocations = new AutoLocations(startPosChooser.getSelected());
    	
        autonomousCommand = new AutonomousCommand(teamChooser.getSelected(), 
        		startPosChooser.getSelected(),
        		autoModeChooser.getSelected(),
        		gameData);

        
        Sensors.gyro.reset();
        Sensors.resetEncoders();
        // Force wrist and gripper to known state
       	Robot.grasper.InitializeForAuto();
    	autonomousCommand.start();
		Constants.IntegralError=0;
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
        Sensors.updateSmartDashboard();
        SmartDashboard.putNumber("Wheel Angle", getWheelAngle());
        SmartDashboard.putNumber("Scaled Auto Speed", Constants.AutoStraighLineSpeedFactor * Constants.AutoDriveSpeed);
    }

    public void teleopInit() {
    	mode = "teleop";
        if (autonomousCommand != null) {
        	autonomousCommand.cancel();        	
        }        
    //	Sensors.resetEncoders();
        Sensors.gyro.reset();
    	elevator.StopMoving();
        Robot.accumulatedHeading = 0;
		Robot.drivetrain.Relax();

		Constants.AutoEnable=true;
		Constants.IntegralError=0;

    	
    }
    
    public static boolean isPracticeRobot() {
    	return (! Sensors.practiceRobotJumperPin.get());
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
//		LiveWindow.run();
        
		
        //if (debugModeChooser.getSelected() == "debug") 
        //{
	        Sensors.updateSmartDashboard();
	        elevator.updateSmartDashboard();
	        climber.updateSmartDashboard();
	        grasper.updateSmartDashboard();
	        
	        SmartDashboard.putNumber("Wheel Angle", getWheelAngle());
	        SmartDashboard.putNumber("Scaled Auto Speed", Constants.AutoStraighLineSpeedFactor * Constants.AutoDriveSpeed);
        //}
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
  //      LiveWindow.run();
    }
}
