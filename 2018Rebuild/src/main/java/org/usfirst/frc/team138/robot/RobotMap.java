package org.usfirst.frc.team138.robot;
/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {
	
	// CAN Bus Assignments
	public final static int LEFT_MOTOR_CHANNEL_FRONT    = 7;
	public final static int LEFT_MOTOR_CHANNEL_BACK     = 8;
	public final static int RIGHT_MOTOR_CHANNEL_FRONT   = 5;
	public final static int RIGHT_MOTOR_CHANNEL_BACK    = 6;
	public final static int LEFT_CUBE_CAN_GRASPER_PORT  = 3;
	public final static int RIGHT_CUBE_CAN_GRASPER_PORT = 4;
  
	public final static int ELEVATOR_PORT 				= 2;
	public final static int WINCH_PORT 					= 1;
	
	// Analog Input
	
	// Pneumatic Control Module
	public final static int SOLENOID_GRASPER_PORT  = 0;
	public final static int SOLENOID_WRIST_PORT    = 1;
	
	// Relay
}
