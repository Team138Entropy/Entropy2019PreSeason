package org.usfirst.frc.team138.robot.commands;

import org.usfirst.frc.team138.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;

public class CancelElevatorMove extends Command {
	
	public CancelElevatorMove()
	{
		requires(Robot.elevator);
		}
	

	protected void initialize() {
		Robot.elevator.StopMoving();
	}

	protected void execute() {
	}

	protected boolean isFinished() {
		return true;
	}

	protected void end() {
		
	}

	protected void interrupted() {
		
	}

}