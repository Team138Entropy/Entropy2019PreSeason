package org.usfirst.frc.team138.robot.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;
//import edu.wpi.first.wpilibj.Ultrasonic;
//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DistanceSensor {
	
	private AnalogInput MySensor = new AnalogInput(0);
	
	 public double GetDistance(){
		 
		double Voltage = MySensor.getVoltage();
		double Distance = (Voltage / .0049) * .3937; //This is distance in cm converted to inch
		return Distance;
	 }
}
