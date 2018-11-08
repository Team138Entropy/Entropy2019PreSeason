package org.usfirst.frc.team138.robot;


import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.SensorCollection;
import edu.wpi.first.wpilibj.DigitalInput;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.CameraServer;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;

import edu.wpi.first.wpilibj.Joystick;
//import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Sensors {
	public static ADXRS450_Gyro gyro; 
	
    static Joystick driverStick = new Joystick(0);
	
	public static SensorCollection leftSensorCollection;
	public static SensorCollection rightSensorCollection;
	
	static UsbCamera Camera0;
	
	public static double gyroBias=0;

	public static DigitalInput practiceRobotJumperPin;
	
	public static void initialize() {
		Robot.drivetrain.frontLeftTalon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
		Robot.drivetrain.frontRightTalon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);

        gyro = new ADXRS450_Gyro();
        gyro.calibrate();
        gyro.reset();

        Camera0 = CameraServer.getInstance().startAutomaticCapture("Camera0", 0);
	// JeVois camera selects which script to run based on VideoMode
	// This VideoMode selects the TestPython.py script which, in the default load, slightly decorates the
	// image with a circle and a string "Hi from Python", but otherwise passes through the camera image unchanged.
	   Camera0.setVideoMode(PixelFormat.kMJPEG, 320, 240, 15);
	   SmartDashboard.putString("PixelFormat", "MJPEG");
	   SmartDashboard.putNumber("x-resolution", 320);
	   SmartDashboard.putNumber("y-resolution",240);     
	   SmartDashboard.putNumber("framerate",30);
	   
	   practiceRobotJumperPin = new DigitalInput(5);
	}
	
	public static double getLeftDistance() {
		// In METERS
		return -Robot.drivetrain.frontLeftTalon.getSelectedSensorPosition(0)*Constants.MetersPerPulse;
	}
	
	public static double getRightDistance() {
		// In METERS
		return -Robot.drivetrain.frontRightTalon.getSelectedSensorPosition(0)*Constants.MetersPerPulse;
	}
	
	public static void resetEncoders() {
		Robot.drivetrain.frontLeftTalon.setSelectedSensorPosition(0, 0, 0);
		Robot.drivetrain.frontRightTalon.setSelectedSensorPosition(0, 0, 0);
	}	
	
	public static void updateSmartDashboard(){
		SmartDashboard.putNumber("Left Pos(M)", getLeftDistance());
		SmartDashboard.putNumber("Right Pos(M)", getRightDistance());
		SmartDashboard.putNumber("Elev Position", Robot.elevator._elevatorMotor.getSelectedSensorPosition(0));     
		SmartDashboard.putNumber("Elev Velocity", Robot.elevator._elevatorMotor.getSelectedSensorVelocity(0));
		
		SmartDashboard.putNumber("Target Heading", Robot.accumulatedHeading);		
		SmartDashboard.putNumber("Robot Heading", gyro.getAngle());
		SmartDashboard.putNumber("Left Velocity",-Robot.drivetrain.frontLeftTalon.getSelectedSensorVelocity(0)*10*Constants.MetersPerPulse);
		SmartDashboard.putNumber("Right Velocity",-Robot.drivetrain.frontRightTalon.getSelectedSensorVelocity(0)*10*Constants.MetersPerPulse);
	}
}
