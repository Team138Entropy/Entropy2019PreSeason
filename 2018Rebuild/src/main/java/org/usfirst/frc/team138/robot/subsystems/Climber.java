package org.usfirst.frc.team138.robot.subsystems;

import org.usfirst.frc.team138.robot.RobotMap;
import org.usfirst.frc.team138.robot.commands.Climb;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 */
public class Climber extends Subsystem {

	public WPI_TalonSRX _winchMotor = new WPI_TalonSRX(RobotMap.WINCH_PORT);
	
	private boolean _isClimbAllowed = false;
	
	
    // Put methods for controlling this subsystem
    // here. Call these from Commands.

	public void set_isClimbAllowed(boolean isClimbAllowed) {
		this._isClimbAllowed = isClimbAllowed;
		
		if (_isClimbAllowed)
		{
			setDefaultCommand(new Climb());
		} else
		{
			setDefaultCommand(null);
		}
	}

	public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new Climb());
    }
    
    public boolean isClimbAllowed() {
    	return _isClimbAllowed;
    }
    
    // Takes values from -1.0 to 1.0
    public void climb(double climbSpeed)
    {
    	if (climbSpeed >= -1.0 && climbSpeed <= 1.0) {
    		_winchMotor.set(ControlMode.PercentOutput, climbSpeed);
    		SmartDashboard.putNumber("Climb Speed", climbSpeed);
    	}
    }
    
    public void updateSmartDashboard()
	{
		SmartDashboard.putNumber("Winch Speed", _winchMotor.getMotorOutputPercent());
	}
    
}