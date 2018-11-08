package org.usfirst.frc.team138.robot.commands;

import org.usfirst.frc.team138.robot.Constants;
import org.usfirst.frc.team138.robot.Robot;
import org.usfirst.frc.team138.robot.subsystems.Elevator.ElevatorTarget;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AutonomousCommand extends CommandGroup {

	public AutonomousCommand(String team, String startPos, String autoMode, String gameData) {
		
		
		// Test Modes
		// Never go into test mode on competition robot
		if (autoMode == "test" && Constants.AutoEnable) //&& Constants.practiceBot)
		{
			if (startPos.equals("left") || startPos.equals("right")) {
				addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, 500));
			}
		}

		
		// This auto mode does the "proper action" depending on the starting position and gameData
		// Always go into real autonomous mode for the competition robot
		// if (!Constants.practiceBot || (autoMode == "auto" && Constants.AutoEnable))
		if (autoMode == "auto" && Constants.AutoEnable)
		{
			Constants.AutoEnable=false;
			SmartDashboard.putString("Auto", "auto");
			if (startPos.equals("left")) {
				
				if (gameData.equals("LLL")) { 
					// Cubes on near scale and switch
					// "Left" angles are inverted
					depositCubeNearScale("Switch");
				}		
				
				if (gameData.equals("RLR")) {
					// Put 2 cubes on near scale
					depositCubeNearScale("Scale");
				}
				
				if (gameData.equals("RRR") ) {
					// Cubes on Far scale and Switch
					depositCubeFarScale("Switch");
				}
				
				if (gameData.equals("LRL") ) {
					// 2 cubes on Far scale
					depositCubeFarScale("Scale");
				}
				
			}
			
			if (startPos.equals( "middle") ) {
				
				if (gameData.equals("LLL") || gameData.equals("LRL") ) 					
					depositCubeLeftSwitch(); // Left Switch
								
				if (gameData.equals("RRR") || gameData.equals( "RLR") ) 					
					depositCubeRightSwitch(); // Right Switch
			
			}
			
			if (startPos.equals("right") ) {
				if (gameData.equals("LLL") ) {
					depositCubeFarScale("Switch");
				}
				
				if (gameData.equals("RRR")) {
					depositCubeNearScale("Switch");
				}
				
				if (gameData.equals("LRL") ) {
					depositCubeNearScale("Scale");
				}
				
				if (gameData.equals( "RLR") ) {
					depositCubeFarScale("Scale");
				}
			}
		}
	}

/*	private void crossAutoLine() {
		// "Off" position
		addSequential(new AutoDrive(Constants.autoSpeed, Constants.distanceBaseLine));
	}
*/
	private void depositCubeNearScale(String Target2)
	{
		// Near Scale
		// Lift and 
		addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(0, 1)));									
		addParallel(new ElevateToTarget(ElevatorTarget.EXCHANGE));
		addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor,Robot.autoLocations.getDistanceByLocations(0, 1)));				 
		// Point towards Scale & Elevate
		addParallel(new ElevateToTarget(ElevatorTarget.UPPER_SCALE));
		addSequential(new AutoDrive(Robot.autoLocations.getAngleByLocation(1)));
		addSequential(new Wait(Constants.wristDelay));
		// Deposit on Scale
		addSequential(new StartRelease());
		addSequential(new Wait(Constants.releaseDelay));
		addSequential(new CompleteRelease());
		// Grab 2nd cube at end of near switch
		// drop elevator to acquire position
		addParallel(new ElevateToTarget(ElevatorTarget.ACQUIRE));
		// Change heading to point towards 2nd cube pickup
		addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(1, 2)));
		addSequential( new ReadyToAcquire());
		addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(1,2)));
		 // Grab cube
		addSequential(new StartAcquire());
		addSequential(new Wait(Constants.acquireDelay));
		addSequential(new CompleteAcquire());
		if (Target2=="Switch") {
			// Elevate
			addSequential(new ElevateToTarget(ElevatorTarget.SWITCH));
			// Drive a little closer
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(2,3)));
			// Deposit on Switch
		}
		else
		{ // Deposit on Scale
			addParallel(new ElevateToTarget(ElevatorTarget.EXCHANGE));
			// Rotate back towards scale
			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(2, 4)));
			// Drive towards scale, while raising elevator
			addParallel(new ElevateToTarget(ElevatorTarget.UPPER_SCALE));
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(2,4)));
			// Re-orient - may remove to save time
			addSequential(new AutoDrive(Robot.autoLocations.getAngleByLocation(4)));
			addSequential(new Wait(Constants.wristDelay));
		}
		// Deposit cube
		addSequential(new StartRelease());
		addSequential(new Wait(Constants.releaseDelay));
		addSequential(new CompleteRelease());
		
	}

	
	private void depositCubeFarScale(String Target2)
	{
			// Far Scale
			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(0, 5)));
			// Elevate while driving towards first waypoint
			addParallel(new ElevateToTarget(ElevatorTarget.EXCHANGE));
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(0,5)));
			// Turn towards far side
			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(5, 6)));
			// Drive across field
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(5,6)));
			// Turn towards Scale
			addParallel(new ElevateToTarget(ElevatorTarget.UPPER_SCALE));
			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(6, 7)));
			// Drive fwd towards scale
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(6,7)));
			addSequential(new Wait(Constants.wristDelay));
			
			// Deposit 1st cube on scale
			addSequential(new StartRelease());
			addSequential(new Wait(Constants.releaseDelay));
			addSequential(new CompleteRelease());
//			// 
//			// Grab 2nd cube at end of far switch
//			// drop elevator to acquire position
//			addParallel(new ElevateToTarget(ElevatorTarget.ACQUIRE));
//			// Turn towards 2nd cube to pickup
//			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(7, 8)));
//			// Drive towards cube
//			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(7,8)));
//			// Grasp 2nd cube
//			addSequential(new StartAcquire());
//			addSequential(new CompleteAcquire());
//			if (Target2=="Switch") {
//				addSequential(new ElevateToTarget(ElevatorTarget.SWITCH));
//				// Drive a little closer
//				addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(8,9)));
//				// Deposit on Switch
//			}
//			else
//			{ // Deposit on Scale
//				addParallel(new ElevateToTarget(ElevatorTarget.UPPER_SCALE));
//				addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(8, 7)));
//				addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(8,7)));
//			}
//			addSequential(new StartRelease());
//			addSequential(new Wait(Constants.releaseDelay));
//			addSequential(new CompleteRelease());
//			addSequential(new CloseGrasper());
		}
	
	
	private void depositCubeLeftSwitch()
	{
			// Center start
			addParallel(new ElevateToTarget(ElevatorTarget.SWITCH));
			// Drive fwd slightly
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(10,13)));
			// Turn towards left waypoint
			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(13, 14)));
			// Drive to waypoint
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(13,14)));
			// Turn towards Switch
			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(14, 11)));
			// Drive Fwd to Switch
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(14,11)));
			// Deposit cube on switch
			addSequential(new StartRelease());
			addSequential(new Wait(Constants.releaseDelay));
			addSequential(new CompleteRelease());
			addSequential(new CloseGrasper());
	}
	
	private void depositCubeRightSwitch()
	{
			addParallel(new ElevateToTarget(ElevatorTarget.SWITCH));
			// Drive Fwd slightly
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(10,13)));
			// Turn towards right waypoint
			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(13, 15)));
			// Drive to waypoint
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(13,15)));
			// Turn towards right switch
			addSequential(new AutoDrive(Robot.autoLocations.getHeadingByLocations(15, 12)));
			// Drive Fwd to deposit on R switch
			addSequential(new AutoDrive(Constants.AutoStraighLineSpeedFactor, Robot.autoLocations.getDistanceByLocations(15,12)));
			// Deposit cube
			addSequential(new StartRelease());
			addSequential(new Wait(Constants.releaseDelay));
			addSequential(new CompleteRelease());
			addSequential(new CloseGrasper());
	}
	

}