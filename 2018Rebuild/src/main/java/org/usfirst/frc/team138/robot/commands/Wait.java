package org.usfirst.frc.team138.robot.commands;

import edu.wpi.first.wpilibj.command.Command;

public class Wait extends Command{
	
	public Wait(double duration){
		setTimeout(duration);
	}

	protected void initialize() {
	}

	protected void execute() {
	}

	protected boolean isFinished() {
		return isTimedOut();
	}

	protected void end() {
	}

	protected void interrupted() {
	}

}
