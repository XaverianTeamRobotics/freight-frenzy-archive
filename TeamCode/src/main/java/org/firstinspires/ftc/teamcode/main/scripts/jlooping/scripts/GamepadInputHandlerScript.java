package org.firstinspires.ftc.teamcode.main.scripts.jlooping.scripts;

import com.michaell.looping.ScriptParameters;
import com.michaell.looping.builtin.ConditionalScriptTemplate;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.main.utils.gamepads.GamepadManager;
import org.firstinspires.ftc.teamcode.main.utils.helpers.elevator.ElevatorDriver;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardColorSensor;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardMotor;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardServo;
import org.firstinspires.ftc.teamcode.main.utils.io.InputSpace;
import org.firstinspires.ftc.teamcode.main.utils.io.OutputSpace;
import org.firstinspires.ftc.teamcode.main.utils.locations.DuckMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorBottomLimitSwitchLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorLeftLiftMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.ElevatorRightLiftMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.HandSpinningServoLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.IntakeLiftingServoLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.IntakeSpinningMotorLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.LeftHandGrabbingServoLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.RightHandGrabbingServoLocation;
import org.firstinspires.ftc.teamcode.main.utils.locations.TankDrivetrainLocation;

public class GamepadInputHandlerScript extends ConditionalScriptTemplate {
    private LinearOpMode opMode;

    private GamepadManager gamepadManager;
    private InputSpace inputSpace;
    private OutputSpace outputSpace;
    private boolean intakeShouldBeDown = false, intakeButtonWasDown = false, isAllowedToControlElevator = false, elevatorButtonWasDown = false, elevatorShouldBeManuallyControlled = false;
    private ElevatorDriver elevatorDriver;

    private int testSpinPos = 0;

    public GamepadInputHandlerScript(String name, LinearOpMode opMode) {
        super(name, true);
        this.opMode = opMode;
    }

    public LinearOpMode getOpMode() {
        return opMode;
    }

    @Override
    public void init(ScriptParameters scriptParameters) {
        gamepadManager = new GamepadManager(getOpMode().gamepad1, getOpMode().gamepad2, getOpMode().gamepad2, getOpMode().gamepad1, getOpMode().gamepad1, getOpMode().gamepad2);
        getOpMode().gamepad1.reset();
        getOpMode().gamepad2.reset();
        // setup control spaces
        inputSpace = new InputSpace(getOpMode().hardwareMap);
        outputSpace = new OutputSpace(getOpMode().hardwareMap);
        // setup elevator driver
        elevatorDriver = new ElevatorDriver(inputSpace, outputSpace, getOpMode());
        elevatorDriver.setFeedbackDestination(gamepadManager);
        elevatorDriver.setManualController(gamepadManager);
        elevatorDriver.setIntakeToggleController(gamepadManager);
        // put everything in their default positions, or auto-calibration
        inputSpace.sendInputToLeftHandGrabber(LeftHandGrabbingServoLocation.Action.SET_POSITION, 90);
        inputSpace.sendInputToRightHandGrabber(RightHandGrabbingServoLocation.Action.SET_POSITION, 37);
        inputSpace.sendInputToHandSpinner(HandSpinningServoLocation.Action.SET_POSITION, 23);
        calibrateElevator();
        /*
         * VALUES OF INTAKE LIFTER:
         * LOW: 30
         * HIGH: 70
         * */
        inputSpace.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 70);
        // alert drivers robot is ready
        gamepadManager.functionOneGamepad().rumble(1000);
        gamepadManager.functionTwoGamepad().rumble(1000);
        gamepadManager.functionThreeGamepad().rumble(1000);
        gamepadManager.functionFourGamepad().rumble(1000);
        gamepadManager.functionFiveGamepad().rumble(1000);
        gamepadManager.functionSixGamepad().rumble(1000);

        needsInit = false;
    }

    @Override
    public void toRun(ScriptParameters scriptParameters) {
        controlDrivetrain();
        controlIntakeLifter();
        controlIntake();
        controlEntireLiftAutonomously();
        controlDuck();
        updateLiftControlPermissions();
    }

    @Override
    public boolean shouldRun(ScriptParameters scriptParameters) {
        try {
            ScriptParameters.GlobalVariable<Boolean> var = scriptParameters.getGlobalVariable("driveEnabled");
            if (var.getValue()) {
                return true;
            } else {
                inputSpace.stop();
                outputSpace.stop();
                return false;
            }
        } catch (ScriptParameters.VariableNotFoundException e) {
            return true;
        }
    }

    private void calibrateElevator() {
        // move elevator up for a second
        int timeAsOfLastElevatorCalibrationBegin = (int) getOpMode().time;
        while(outputSpace.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) == 0 && timeAsOfLastElevatorCalibrationBegin > (int) getOpMode().time - 1) {
            inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, -100);
            inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, -100);
        }
        // move elevator down until it reaches the bottom
        while(outputSpace.receiveOutputFromElevatorBottomLimitSwitch(ElevatorBottomLimitSwitchLocation.Values.PRESSED) == 0) {
            inputSpace.sendInputToElevatorLeftLift(ElevatorLeftLiftMotorLocation.Action.SET_SPEED, 30);
            inputSpace.sendInputToElevatorRightLift(ElevatorRightLiftMotorLocation.Action.SET_SPEED, 30);
        }
        // reset the elevator
        ((StandardMotor) inputSpace.getElevatorLeftLift().getInternalInteractionSurface()).reset();
        ((StandardMotor) inputSpace.getElevatorRightLift().getInternalInteractionSurface()).reset();
    }

    private void controlDrivetrain() {
        // calculate the x and y speeds
        int modifier = 90;
        int left = (int) Range.clip((gamepadManager.functionOneGamepad().left_stick_y - gamepadManager.functionOneGamepad().right_stick_x) * modifier, -modifier, modifier);
        int right = (int) Range.clip((gamepadManager.functionOneGamepad().left_stick_y + gamepadManager.functionOneGamepad().right_stick_x) * modifier, -modifier, modifier);
        // set the defined speeds
        inputSpace.sendInputToTank(TankDrivetrainLocation.Action.SET_SPEED, -right, -left);
    }

    private void controlIntakeLifter() {
        // move the intake based on the left bumper's state
        if(gamepadManager.functionTwoGamepad().left_bumper) {
            if(!intakeButtonWasDown) {
                intakeShouldBeDown = !intakeShouldBeDown;
            }
            intakeButtonWasDown = true;
        }else{
            intakeButtonWasDown = false;
        }
        if(intakeShouldBeDown) {
            inputSpace.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 30);
        }else{
            inputSpace.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 70);
        }
    }

    public void updateLiftControlPermissions() {
        isAllowedToControlElevator = ((StandardServo) inputSpace.getIntakeLifter().getInternalInteractionSurface()).getPosition() != 70;
        if(!elevatorDriver.isStable()) {
            inputSpace.sendInputToIntakeLifter(IntakeLiftingServoLocation.Action.SET_POSITION, 30);
        }
    }

    private void controlIntake() {
        // control the intake motor based on the trigger inputs
        int modifier = 100;
        int intakeGas = (int) Range.clip(gamepadManager.functionTwoGamepad().right_trigger * modifier, 0, modifier);
        int intakeBrake = (int) Range.clip(gamepadManager.functionTwoGamepad().left_trigger * modifier, 0, modifier);
        int intakeSpeed = Range.clip(intakeGas - intakeBrake, -100, 100);
        inputSpace.sendInputToIntakeSpinner(IntakeSpinningMotorLocation.Action.SET_SPEED, intakeSpeed);
    }

    /**
     * This method controls all the autonomous stuff for the lift in TeleOps. Basically, it contains a bunch of routines. On every run, if no routine is running and a button is pressed to toggle a certain routine, the routine will fire. It will enable its routine, making all other routines impossible to run. During running, controllers will give feedback via vibrations to the user to let them know the elevator is performing a routine. Once a routine is complete, they will stop and the elevator will be able to run another routine once input is received.
     */
    private void controlEntireLiftAutonomously() {
        double[] hsv = ((StandardColorSensor) outputSpace.getHandColorSensor().getInternalInteractionSurface()).getHSV();
        int distance = (int) outputSpace.receiveOutputFromHandDistanceSensor();
        elevatorDriver.run();
        if(isAllowedToControlElevator) {
            // enables intake pos routine if requested
            if(gamepadManager.functionThreeGamepad().a) {
                elevatorDriver.setToIntakePosition();
            }
            // enables lower level ball routine if requested
            if(gamepadManager.functionThreeGamepad().b) {
                if(hsv[0] > 130) {
                    elevatorDriver.setToLowerBallPosition();
                }else{
                    elevatorDriver.setToLowerBlockPosition();
                }
            }
            // enables middle level ball routine routine if requested
            if(gamepadManager.functionThreeGamepad().y) {
                if(hsv[0] > 130) {
                    elevatorDriver.setToMediumBallPosition();
                }else{
                    elevatorDriver.setToMediumBlockPosition();
                }
            }
            // enables top level ball routine if requested
            if(gamepadManager.functionThreeGamepad().x) {
                if(hsv[0] > 130) {
                    elevatorDriver.setToTopBallPosition();
                }else{
                    elevatorDriver.setToTopBlockPosition();
                }
            }
            // toggles manual control
            if(gamepadManager.functionTwoGamepad().right_bumper) {
                if(!elevatorButtonWasDown) {
                    elevatorShouldBeManuallyControlled = !elevatorShouldBeManuallyControlled;
                }
                elevatorButtonWasDown = true;
            }else{
                elevatorButtonWasDown = false;
            }
            // enables/disables manual control based on toggle
            if(elevatorShouldBeManuallyControlled) {
                elevatorDriver.enableManualControl();
            }else{
                elevatorDriver.disableManualControl();
            }

            // manual hand
            if (elevatorShouldBeManuallyControlled) {
                if (gamepadManager.functionFiveGamepad().dpad_left) {
                    elevatorDriver.manuallyGrabHand();
                } else if (gamepadManager.functionFiveGamepad().dpad_right) {
                    elevatorDriver.manuallyRealeaseHand();
                }
            }
        }
    }

    private void controlDuck() {
        // turn duck motor slowly in the correct direction
        int speed = gamepadManager.functionFourGamepad().right_bumper ? -50 : 0;
        speed += gamepadManager.functionFourGamepad().left_bumper ? 50 : 0;
        inputSpace.sendInputToDuckMotor(DuckMotorLocation.Action.SET_SPEED, speed);
    }

    private void testManualControl() {

        double s = gamepadManager.functionSixGamepad().left_stick_y * 100;
        int ls = (int) Range.clip(s, -100, 100);
        int rs = (int) Range.clip(s, -100, 100);
        // get hand inputs
        if(gamepadManager.functionSixGamepad().right_stick_y >= 0.2) {
            testSpinPos -= 1;
        }else if(gamepadManager.functionSixGamepad().right_stick_y <= -0.2) {
            testSpinPos += 1;
        }
        // make sure theyre wthin boundaries
        testSpinPos = Range.clip(testSpinPos, 23, 100);
        getOpMode().telemetry.addData("Left Speed: ", ls);
        getOpMode().telemetry.addData("Right Speed: ", rs);
        getOpMode().telemetry.addData("Spin Position: ", testSpinPos);
    }
}
