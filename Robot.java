// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import com.revrobotics.CANSparkMax; //imports library for CAN controlled SparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType; //imports library for SmarkMax control functions (required)
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import edu.wpi.first.wpilibj.Joystick;// imports a library for communicating with a joystick
import com.ctre.phoenix.sensors.CANCoder; 
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  private Joystick translateStick; //names a joystick named driverStick
  private Joystick rotateStick; //names  joystick for rotation
  private CANSparkMax ws1Motor; //wheel speed motor on module 1 (front right)
  private CANSparkMax wa1Motor; //wheel angle motor on module 1 (front right)
  private CANSparkMax ws2Motor; //front left
  private CANSparkMax wa2Motor; //front left
  private CANSparkMax ws3Motor; //rear left
  private CANSparkMax wa3Motor; //rear left
  private CANSparkMax ws4Motor; //rear right
  private CANSparkMax wa4Motor; //rear right
  private CANCoder wa1Encoder; // front right wheel angle module encoder NOT the encoder on the wheel angle motor itself
  private CANCoder wa2Encoder; //angle encoder for front left
  private CANCoder wa3Encoder; //angle encoder for rear left
  private CANCoder wa4Encoder; //angle encoder for rear right
  


//CONSTANTS

//length (L) and width (W) and angle adjustment (R)
  public final int L=24;
  public final int W=24;
  public final double R=Math.sqrt((Math.pow(L, 2))+(Math.pow(W, 2)));

//running constants
  double dbS = .05; //deadband sensitivity 
  double kPangle = .005; //angle motor proportional constant strength
  

  @Override
  public void robotInit() {
//instantiate joysticks
  translateStick = new Joystick(0); //creates a new joystick on USB input 0
  rotateStick = new Joystick(1); //creates a joystick on USB input 1
//instantiate motors
 ws1Motor = new CANSparkMax(7, MotorType.kBrushless); //creates a CAN speed controller for module 1 motors (front right)
 wa1Motor = new CANSparkMax(8, MotorType.kBrushless); //FR angle motor
 ws2Motor = new CANSparkMax(11, MotorType.kBrushless); //FL speed motor
 wa2Motor = new CANSparkMax(12, MotorType.kBrushless); //FL angle motor
 ws3Motor = new CANSparkMax(13, MotorType.kBrushless); //RR speed motor
 wa3Motor = new CANSparkMax(14, MotorType.kBrushless); //RR angle motor
 ws4Motor = new CANSparkMax(5, MotorType.kBrushless); //RL speed motor
 wa4Motor = new CANSparkMax(6, MotorType.kBrushless); //RR angle motor





//instantiate encoders
 wa1Encoder = new CANCoder(8); // creates a new CANCoder with ID 8 for the front right module
 wa2Encoder = new CANCoder(12); //FL CANCoder
 wa3Encoder = new CANCoder(14); //RL CANCoder
 wa4Encoder = new CANCoder(6); //RR CANCoder
  }

  @Override
  public void robotPeriodic() {
  }

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {
//get joystick inputs, forward, strafe, and rotate clockwise and apply a simple deadband

    double FWD =-translateStick.getY();
    if(FWD<dbS && FWD >-dbS){FWD=0;}
    double STR = translateStick.getX();
    if(STR<dbS && STR >-dbS){STR=0;}
    double RCW = rotateStick.getX();
    if(RCW<dbS && RCW >-dbS){RCW=0;}

//compute inverse kinamatics variables
    double A= ((STR-RCW)*(L/R));
    double B= ((STR+RCW)*(L/R));
    double C= ((FWD-RCW)*(W/R));
    double D= ((FWD+RCW)*(W/R));

//compute wheel speeds and angles 1=front right, 2=front left, 3=rear left 4=rear right
    double ws1=Math.sqrt((Math.pow(B, 2))+(Math.pow(C, 2)));  //math.pow squares the varible
    double ws2=Math.sqrt((Math.pow(B, 2))+(Math.pow(D, 2)));
    double ws3=Math.sqrt((Math.pow(A, 2))+(Math.pow(D, 2)));
    double ws4=Math.sqrt((Math.pow(A, 2))+(Math.pow(C, 2)));

//wheel angles computed from -180 to +180, measured clockwise with 0=straight ahead
    double wa1=(Math.atan2(B, C))*(180/Math.PI);
    double wa2=(Math.atan2(B, D))*(180/Math.PI);
    double wa3=(Math.atan2(A, D))*(180/Math.PI);
    double wa4=(Math.atan2(A, C))*(180/Math.PI);

//normalize wheel speeds to prevent overruns
double max=ws1;
  if(ws2>max){max=ws2;}
  if(ws3>max){max=ws3;}
  if(ws4>max){max=ws4;}
  if(max>1){ws1/=max; ws2/=max; ws3/=max; ws4/=max;}
//all wheel speeds are now computed from 0 to 1


//set wheel speed motors
  ws1Motor.set(ws1);
  ws2Motor.set(ws2);
  ws3Motor.set(ws3);
  ws4Motor.set(ws4);


//set wheel angles
//set wheel angle by computing difference from current angle to desired angle and running through P loop to control
  wa1Motor.set((wa1Encoder.getPosition()-wa1)*kPangle);
  wa2Motor.set((wa2Encoder.getPosition()-wa2)*kPangle);
  wa3Motor.set((wa3Encoder.getPosition()-wa3)*kPangle);
  wa4Motor.set((wa4Encoder.getPosition()-wa4)*kPangle);

//debug information
  SmartDashboard.putNumber("wa1 Actual Position", wa1Encoder.getPosition());
  SmartDashboard.putNumber("wa1 Desired Position", wa1);
  SmartDashboard.putNumber("wa1 Desired Wheel Speed", ws1);  
}

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}
}
