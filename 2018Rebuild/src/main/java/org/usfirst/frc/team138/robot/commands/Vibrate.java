package org.usfirst.frc.team138.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.GenericHID;

public class Vibrate extends Command{
	private GenericHID controller;

	public Vibrate(GenericHID controller) {
        	this.controller = controller;
	}

	protected void initialize() {
	}

	protected void execute() {
        // controller.setRumble(GenericHID.RumbleType.kLeftRumble, 0.5);
       		System.out.println("execute");
	}

	protected boolean isFinished() {
		return isTimedOut();
	}

	protected void end() {
        	System.out.println("end");
	}

	public void start() {
        	System.out.println("start");
	}

	protected void interrupted() {
	}

}
