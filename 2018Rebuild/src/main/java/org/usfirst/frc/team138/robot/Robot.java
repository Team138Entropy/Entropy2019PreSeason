package org.usfirst.frc.team138.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team138.robot.subsystems.*;

/**
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
    SendableChooser<String> debugModeChooser;
        
    // Subsystems
    public static final Drivetrain drivetrain = new Drivetrain();

    public static double accumulatedHeading = 0.0; // Accumulate heading angle (target)
	
    Preferences prefs = Preferences.getInstance();
    
    // Location lookup
    public static AutoLocations autoLocations;
    
    // Globals
    public static String mode; // "auto" or "teleop"

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	drivetrain.driveTrainInit(); 

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
					
		debugModeChooser = new SendableChooser<String>();
		debugModeChooser.addObject("Debug", "debug");
		debugModeChooser.addObject("Competition", "competition");
		SmartDashboard.putData("Debug Mode:", debugModeChooser);
		
		SmartDashboard.putBoolean("practiceBot", Constants.practiceBot);		
        Robot.accumulatedHeading = 0;
        Constants.AutoEnable= true;

        // *facepalm*
        // Constants.practiceBot = isPracticeRobot();
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
    
    // TODO: AutoDrive was removed, so this needs to be reimplemented.
    /*
	private double getWheelAngle() {
		double wheelAngle = (AutoDrive.rightDistance() - AutoDrive.leftDistance()) / Constants.driveWheelSpacing;
		return wheelAngle * (180 / Math.PI);
    }
    */

    public void autonomousInit() {
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    }

    /**
     * This gets called when we start controlling the robot manually. Try not to delete it.
     */
    public void teleopInit() {
    	mode = "teleop";
        Robot.accumulatedHeading = 0;
		Robot.drivetrain.relax();

		Constants.AutoEnable=true;
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
        
        // NOTE: Does not work. Relies on getWheelAngle, which is broken.
        //SmartDashboard.putNumber("Wheel Angle", getWheelAngle());

        // NOTE: Commented this out because I'm not sure what it's for, and it relies on stuff related to autonomous mode
        // SmartDashboard.putNumber("Scaled Auto Speed", Constants.AutoStraighLineSpeedFactor * Constants.AutoDriveSpeed);
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    }
}

/* A good luck charm
                   ,-'     `._ 
                 ,'           `.        ,-. 
               ,'               \       ),.\ 
     ,.       /                  \     /(  \; 
    /'\\     ,o.        ,ooooo.   \  ,'  `-') 
    )) )`. d8P"Y8.    ,8P"""""Y8.  `'  .--"' 
   (`-'   `Y'  `Y8    dP       `'     / 
    `----.(   __ `    ,' ,---.       ( 
           ),--.`.   (  ;,---.        ) 
          / \O_,' )   \  \O_,'        | 
         ;  `-- ,'       `---'        | 
         |    -'         `.           | 
        _;    ,            )          : 
     _.'|     `.:._   ,.::" `..       | 
  --'   |   .'     """         `      |`. 
        |  :;      :   :     _.       |`.`.-'--. 
        |  ' .     :   :__.,'|/       |  \ 
        `     \--.__.-'|_|_|-/        /   ) 
         \     \_   `--^"__,'        ,    | 
         ;  `    `--^---'          ,'     | 
          \  `                    /      / 
           \   `    _ _          / 
            \           `       / 
             \           '    ,' 
              `.       ,   _,' 
                `-.___.---' 
*/
