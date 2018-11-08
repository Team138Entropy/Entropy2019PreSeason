package org.usfirst.frc.team138.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc.team138.robot.Robot;
import org.usfirst.frc.team138.robot.Sensors;
import org.usfirst.frc.team138.robot.Utility;
import org.usfirst.frc.team138.robot.Constants;


public class AutoDrive extends Command {
	
	boolean isDone = false;
	double rotateToAngleRate = 0.0;
	double lastLeftDistance = 0.0;
	double lastRightDistance = 0.0;
	int stallCounter = 0;
	boolean areMotorsStalled = false;
	boolean rotateInPlace;
	double driveSpeed = 0.0;
	double driveDistance = 0.0;
	boolean arcTurn = false;
	double timer=0;
	double MinDistance=.25*.025*Constants.AutoDriveSpeed*Constants.Meters2CM; // centimeter (limit to detect stall)
	double lclAngle=0;
	boolean FirstTime=false;
	int  turnDir=1; // 1=positive rotation, -1=negative rotation; 0 =straight
	//*******************************************
	
	//Degree Tolerance
	//within how many degrees will you be capable of turning
	static double ToleranceDegrees = 2.0;

	/**
	 * Drives straight for the specified distance
	 * @param speedArg The speed, from 0 to full in Meters/sec, the robot will drive. 
	 * Negative distance drives backwards
	 * @param distanceArg The distance to drive, in CM (centimeters). 
	 */
	public AutoDrive(double speedArg, double distanceArg){
		requires(Robot.drivetrain);
		rotateInPlace = false;
		driveSpeed = Math.abs(speedArg)*Constants.AutoDriveSpeed;
		driveDistance = distanceArg;
		timer=0;
		stallCounter=0;
		isDone=false;
		turnDir=0;
	}	
	/**
	 * Rotates to an angle
	 * @param angle Angle, in degrees, to turn to. Negative angles turn right, positive angles turn left
	 * ie:  right hand rule convention
	 */
	public AutoDrive(double angle){
		requires(Robot.drivetrain);
		rotateInPlace = true;
		lclAngle=angle;
		if (angle>0) {
			turnDir=1;
		}
		else {
			turnDir=-1;			
		}

		stallCounter=0;
		isDone=false;
		FirstTime=true;
	}
	
	
	public static double leftDistance() {
		// Return leftDistance from left encoder in centimeters
		// motors and encoders run opposite to robot convention
		// Invert encoder readings here so that distance increases
		// when robot moving forward.
		return Constants.Meters2CM*Sensors.getLeftDistance();
	}
	
	public static double rightDistance() {
		// Return rightDistance from right encoder in centimeters
		return Constants.Meters2CM*Sensors.getRightDistance();
	}

	public void initialize() {
		Sensors.resetEncoders();
		timer=0;
		stallCounter=0;
	}

	public void execute() {
		boolean moveComplete=false; // true when move complete
		double rate; // rate of rotation			
		double avgDistance=.5*(leftDistance()+rightDistance());
		
		// Stalled?
		double distanceRemaining=Math.abs(driveDistance)-Math.abs(avgDistance);
		if (FirstTime ){
			FirstTime=false;
			Constants.IntegralError=0;
			if (rotateInPlace)
				Robot.accumulatedHeading = lclAngle;
			else
				Sensors.resetEncoders();
		}
		SmartDashboard.putNumber("Auto Angle",Robot.accumulatedHeading );
		/*
		if (Math.abs(lastLeftDistance-leftDistance())<MinDistance || 
				Math.abs(lastRightDistance-rightDistance())<MinDistance ) 
		{
			if (stallCounter == 50) 
			{
				Robot.drivetrain.drive(0.0, 0.0);
				areMotorsStalled = true;
				isDone = true;
			}
			stallCounter++;
			SmartDashboard.putString("Stall:","Stalled");
		}
		else
		*/
		
		{
			stallCounter = 0;
			
			// Angular difference between target and current heading
			// diffAgnles returns "short way 'round" between 2 angles.
			double diffAngle=Robot.accumulatedHeading-Sensors.gyro.getAngle();
			SmartDashboard.putNumber("Diff Angle", diffAngle);
			
			if (rotateInPlace)
			{
				moveComplete =(Math.abs(diffAngle))<=ToleranceDegrees;
				driveSpeed=0;
			}
			else
			{
				// Check distance-to-go
				// Are we there yet?
				moveComplete = (Math.abs(driveDistance) - Math.abs(avgDistance) < Constants.AutoDriveStopTolerance);
			}
			if (moveComplete || isDone)
			{
				Robot.drivetrain.drive(0.0, 0.0);
				isDone = true;
			}
			else {
				double speed;
				Constants.IntegralError+=diffAngle*.025;
				if (driveSpeed == 0) {
					rate=Constants.kPRotate*diffAngle+Constants.IntegralError*Constants.kIRotate-Constants.kDRotate*Sensors.gyro.getRate();
//					rate=-Constants.kPRotate*Sensors.gyro.getAngle()+Constants.IntegralError*Constants.kIRotate-Constants.kDRotate*Sensors.gyro.getRate();
				} else {
					rate=Constants.kPDrive*diffAngle-Constants.kDDrive*Sensors.gyro.getRate();
				}
				// Scale to Meters/second
				rotateToAngleRate=Utility.limitValue(rate,-1,1)*Constants.AutoDriveRotateRate;	
				// Control deceleration to stop based on AutoDriveAccel limit
				// Approaching stop: Speed= sqrt(2*Accel*distance2Go), otherwise: driveSpeed
				distanceRemaining=Math.max(0,Math.abs(distanceRemaining))/Constants.Meters2CM; // Meters
				speed=Math.min(Math.sqrt(2*Constants.AutoDriveAccel*Math.abs(driveSpeed*distanceRemaining)),driveSpeed);
				// speed has sign of driveDistance
				speed=Robot.drivetrain.limitDriveAccel(Math.signum(driveDistance)*speed);
				rotateToAngleRate=Robot.drivetrain.limitRotateAccel(rotateToAngleRate);
				
				Robot.drivetrain.drive(speed, rotateToAngleRate);
			}
		}
		// update distance moved (used to detect stalled motors)
		lastLeftDistance = leftDistance();
		lastRightDistance = rightDistance();
	}

	public boolean isFinished() {
		if (isDone && Robot.elevator.IsMoveComplete()) {
			Robot.drivetrain.drive(0,0);
//			Robot.drivetrain.Relax();
			return true;
		}
		else
			return false;
	}

	public void end() {
	}

	protected void interrupted() {
	}
	
	
}
