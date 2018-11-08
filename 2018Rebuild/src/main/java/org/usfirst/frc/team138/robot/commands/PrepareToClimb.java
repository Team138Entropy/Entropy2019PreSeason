package org.usfirst.frc.team138.robot.commands;

import org.usfirst.frc.team138.robot.Constants;
import org.usfirst.frc.team138.robot.Robot;
import org.usfirst.frc.team138.robot.subsystems.Elevator.ElevatorTarget;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 */
public class PrepareToClimb extends Command {

	private final double commandTimeoutSeconds = 7;
	private double _currentCommandTime = 0;
	
    public PrepareToClimb() {
        requires(Robot.grasper);
        requires(Robot.elevator);
        requires(Robot.climber);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	_currentCommandTime = 0;
    	if (Robot.climber.isClimbAllowed()) {
//    		Robot.grasper.lowerWrist();
//    		Robot.elevator.Elevate(ElevatorTarget.ACQUIRE);
    		Robot.climber.set_isClimbAllowed(false);
    	} else {
    		Robot.grasper.openGrasper();
    		Robot.grasper.raiseWrist();
    		Robot.grasper.stopRollers();
    		Robot.elevator.Elevate(ElevatorTarget.RUNG);
    		Robot.climber.set_isClimbAllowed(true);
    	}
    }

	protected void execute() {
		if (Robot.climber.isClimbAllowed())
		{
			Robot.elevator.Execute();
			_currentCommandTime += Constants.commandLoopIterationSeconds;
			SmartDashboard.putNumber("Timer", _currentCommandTime);
		}
	}

	protected boolean isFinished() {
		if (_currentCommandTime >= commandTimeoutSeconds)
		{
			return true;
		}
		else
		{
			return Robot.elevator.IsMoveComplete();
		}
	}

	protected void end() {
		Robot.elevator.StopMoving();
		
	}

	protected void interrupted() {
		
		Robot.elevator.CancelMove();
	}
}