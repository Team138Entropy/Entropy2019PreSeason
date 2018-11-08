package org.usfirst.frc.team138.robot.subsystems;

import org.usfirst.frc.team138.robot.Constants;
import org.usfirst.frc.team138.robot.RobotMap;
import org.usfirst.frc.team138.robot.commands.AutoAcquire;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Grasper extends Subsystem{
	
	// These are some class variables
	
	private Solenoid _grasperSolenoid = new Solenoid(RobotMap.SOLENOID_GRASPER_PORT);
	private Solenoid _wristSolenoid = new Solenoid(RobotMap.SOLENOID_WRIST_PORT);
	
	// Used for Simulation
	private static boolean _isGrasperOpen = true;
	private static boolean _isWristRaised = true;
	
	public enum RollerState {
		OFF,
		HOLD,
		RELEASE,
		ACQUIRE
	}
	private static RollerState _acquisitionState = RollerState.OFF;
	
	private WPI_TalonSRX _leftRollerTalon = new WPI_TalonSRX(RobotMap.LEFT_CUBE_CAN_GRASPER_PORT);
	private WPI_TalonSRX _rightRollerTalon = new WPI_TalonSRX(RobotMap.RIGHT_CUBE_CAN_GRASPER_PORT);
	
	private static boolean _isCubeDetected = false;
	private static boolean _isCubeReleased = true;
	private static boolean _isCubeAcquired = false;
	
	// Master
	 SpeedControllerGroup _rollerSpeedController = new SpeedControllerGroup(_leftRollerTalon, _rightRollerTalon);
	
	protected void initDefaultCommand() {
		//setDefaultCommand(new AutoAcquire());
	}
	
	public void initialize() {
    	_leftRollerTalon.setInverted(true);
    }
	
	// Grasper Functions
	
	public void toggleGrasper() {
		if (grasperIsOpen()){
			closeGrasper();
		}
		else {
			openGrasper();
		}
	}
	
	public void openGrasper() {
    	_grasperSolenoid.set(Constants.grasperSolenoidActiveOpen);
		_isGrasperOpen = true;
    }
    
    public void closeGrasper() {
    	_grasperSolenoid.set(!Constants.grasperSolenoidActiveOpen);
    	_isGrasperOpen = false;
    }
    
    public boolean grasperIsOpen() {
    	if (Constants.isSimulated)
    	{
    		// For Simulation
    		return _isGrasperOpen;
    	}
    	else
    	{
    		return (_grasperSolenoid.get() == Constants.grasperSolenoidActiveOpen);
    	}
	}
    
    // Wrist Functions
    
    public void toggleWrist() {
    	if (isWristUp()) {
    		lowerWrist();
    	}
    	else {
    		raiseWrist();
    	}
    }
    
    public void InitializeForAuto() {
    	raiseWrist();
    	closeGrasper();
    	holdRollers();
    }
    
    public void raiseWrist() {
    	_wristSolenoid.set(Constants.wristSolenoidActiveRaised);
    	_isWristRaised = true;
    }
    
    public void lowerWrist() {
    	_wristSolenoid.set(!Constants.wristSolenoidActiveRaised);
    	_isWristRaised = false;
    }
	
	public boolean isWristUp() {
		if (Constants.isSimulated)
		{
			// For simulation
			return _isWristRaised;
		}
		else
		{
			return (_wristSolenoid.get() == Constants.wristSolenoidActiveRaised);	
		}
	}
	public boolean isWristDown() {
		return (!isWristUp());
	}
	
	// Acquisition Roller Functions
	public boolean isCubeDetected() {
		if (Constants.isSimulated)
		{
			//For Simulation
			return _isCubeDetected;
		}
		else
		{
			return (_leftRollerTalon.getOutputCurrent() > Constants.cubeDetectThreshold || 
					_rightRollerTalon.getOutputCurrent() > Constants.cubeDetectThreshold);
		}	
	}
	public boolean isCubeAcquired() {
		if (Constants.isSimulated)
		{
			//For Simulation
			return _isCubeAcquired;
		}
		else
		{
			return (_leftRollerTalon.getOutputCurrent() > Constants.cubeAcquireThreshold || 
					_rightRollerTalon.getOutputCurrent() > Constants.cubeAcquireThreshold);
		}
	}
	
	public boolean isCubeManuallyAcquired() {
		return _isCubeAcquired;
	}
	
	public void toggleDetectCube() {
		if (_isCubeDetected) {
			_isCubeDetected = false;
		}
		else {
			_isCubeDetected = true;
		}
	}
	public void toggleAcquireCube() {
		if (_isCubeAcquired) {
			_isCubeAcquired = false;
		}
		else {
			_isCubeAcquired = true;
		}
	}
	
	public void acquireRollers() {
		_rollerSpeedController.set(Constants.aquireSpeed);
		_acquisitionState =  RollerState.ACQUIRE;
	}
	
	private void releaseRollers() {
		if (isWristDown())
		{
			_rollerSpeedController.set(Constants.releaseSpeedReduced);
		}
		else
		{
			_rollerSpeedController.set(Constants.releaseSpeed);
		}
		_acquisitionState =  RollerState.RELEASE;
	}
	
	private void holdRollers() {
		_rollerSpeedController.set(Constants.holdSpeed);
		_acquisitionState =  RollerState.HOLD;
	}
	
	public void stopRollers() {
		_rollerSpeedController.set(0);
		_acquisitionState =  RollerState.OFF;
	}
	
	public boolean isRollerState(RollerState rollerState) {
		return (_acquisitionState == rollerState);
	}
	
	public void toggleRollers()
	{
		switch (_acquisitionState) {
			case OFF: 
				acquireRollers();
			break;
			case ACQUIRE:
				holdRollers();
			break;
			case HOLD:
				releaseRollers();
				break;
			case RELEASE:
			default:
				stopRollers();
				break;
		}
	}
	
	
	// Command Functions
	
	public void StartAcquire() {
		SmartDashboard.putString("Acquire Release","Start Acquire");
		closeGrasper();
		acquireRollers(); 
		_isCubeReleased = false;
		_isCubeAcquired = false;
	}
	
	public void CompleteAcquire() {
		SmartDashboard.putString("Acquire Release", "Complete Acquire");
		holdRollers();
		_isCubeReleased = false;
		_isCubeAcquired = true;
	}

	public void StartRelease() {
		SmartDashboard.putString("Acquire Release","Start Release");
		releaseRollers();
		lowerWrist();
		_isCubeReleased = false;
		_isCubeAcquired = false;
	}
	
	public void CompleteRelease() {
		SmartDashboard.putString("Acquire Release","Complete Release");
		stopRollers();
		openGrasper();
		_isCubeReleased = true;
		_isCubeAcquired = false;
	}
	
	public boolean isCubeReleased() {
		return _isCubeReleased;
	}
	
	public void updateSmartDashboard()
	{
		if (grasperIsOpen()) {
			SmartDashboard.putString("Grasper", "open");
		}
		else {
			SmartDashboard.putString("Grasper", "closed");
		}
		
		if (isWristUp()) {
			SmartDashboard.putString("Wrist", "raised");
		}
		else {
			SmartDashboard.putString("Wrist", "lowered");
		}
		
		SmartDashboard.putBoolean("Cube Detected", isCubeDetected());
		SmartDashboard.putBoolean("Cube Acquired", isCubeAcquired());
		SmartDashboard.putString("Acquisition Wheels", _acquisitionState.toString());
		SmartDashboard.putNumber("Roller Speed", _rollerSpeedController.get());
		SmartDashboard.putNumber("L Acquisition Current", _leftRollerTalon.getOutputCurrent());
		SmartDashboard.putNumber("R Acquisition Current", _rightRollerTalon.getOutputCurrent());
	}
}
