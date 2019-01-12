package org.usfirst.frc.team138.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.GenericHID.*;

public class Vibrate extends Command{
	
	public Vibrate(GenericHID controller){
        this.controller = controller;
	}

	protected void initialize() {
	}

	protected void execute() {
        // this.controller.setRumble(GenericHID.RumbleType.kLeftRumble, 0.5);
        System.out.printlnt("execute");
	}

	protected boolean isFinished() {
		return isTimedOut();
	}

	protected void end() {
        System.out.printlnt("end");
	}

	protected void start() {
        System.out.printlnt("start");
	}

	protected void interrupted() {
	}

}
