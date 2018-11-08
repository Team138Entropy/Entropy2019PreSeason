package org.usfirst.frc.team138.robot.commands;

import org.usfirst.frc.team138.robot.OI;
import org.usfirst.frc.team138.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ControlWinch extends Command {
	
	public ControlWinch()
	{
		requires(Robot.climber);
	}
	

	protected void initialize() {
		
	}

	protected void execute() {
		Robot.climber.climb(OI.getWinchSpeed());
	}

	protected boolean isFinished() {
		return false;
	}

	protected void end() {
		Robot.climber.climb(0.0);
	}

	protected void interrupted() {
		Robot.climber.climb(0.0);
	}

}