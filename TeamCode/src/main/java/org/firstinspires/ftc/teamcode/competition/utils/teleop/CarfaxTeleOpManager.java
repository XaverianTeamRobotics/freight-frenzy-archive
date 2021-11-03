package org.firstinspires.ftc.teamcode.competition.utils.teleop;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.R;
import org.firstinspires.ftc.teamcode.competition.utils.Carfax;
import org.firstinspires.ftc.teamcode.competition.utils.Motor;

public class CarfaxTeleOpManager extends TeleOpManager {

    private final Carfax CARFAX;
    private final Motor SPINNER, LIFT;
    private final TeleOpHWDevices DEVICES;

    public CarfaxTeleOpManager(Telemetry telemetry, HardwareMap hardwareMap, Gamepad gamepad1, Gamepad gamepad2, GamepadFunctions function1, GamepadFunctions function2, TeleOpHWDevices devices) {
        super(gamepad1, function1, gamepad2, function2);
        CARFAX = new Carfax(telemetry, new Motor(telemetry, hardwareMap, hardwareMap.appContext.getString(R.string.DRIVETRAIN_RIGHT_DRIVE_1), DcMotorSimple.Direction.FORWARD), new Motor(telemetry, hardwareMap, hardwareMap.appContext.getString(R.string.DRIVETRAIN_LEFT_DRIVE_1), DcMotorSimple.Direction.REVERSE));
        if(devices.isSpinnerMotorAllowed()) {
            SPINNER = new Motor(telemetry, hardwareMap, hardwareMap.appContext.getString(R.string.HW_SPINNER), DcMotorSimple.Direction.FORWARD);
        }else{
            SPINNER = null;
        }
        if(devices.isLiftMotorAllowed()) {
            LIFT = new Motor(telemetry, hardwareMap, hardwareMap.appContext.getString(R.string.HW_LIFT), DcMotorSimple.Direction.FORWARD);
        }else{
            LIFT = null;
        }
        DEVICES = devices;
    }

    /**
     * Show me the Carfax™.
     */
    @Override
    public void main() {
        if(getGamepad1Functions().hasF1()) {
            CARFAX.driveWithEncoder((int) Range.clip((-getGamepad1().left_stick_y + getGamepad1().left_stick_x) * 100, -100, 100), (int) Range.clip((-getGamepad1().left_stick_y - getGamepad1().left_stick_x) * 100, -100, 100));
        }else if(getGamepad2Functions().hasF1()) {
            CARFAX.driveWithEncoder((int) Range.clip((-getGamepad2().left_stick_y + getGamepad2().left_stick_x)  * 100, -100, 100), (int) Range.clip((-getGamepad2().left_stick_y - getGamepad2().left_stick_x) * 100, -100, 100));
        }
        if(getGamepad1Functions().hasF2()) {
            if(DEVICES.isSpinnerMotorAllowed()) {
                SPINNER.driveWithEncoder((int) Range.clip((getGamepad1().left_trigger - getGamepad1().right_trigger) * 100, -100, 100));
            }
            if(DEVICES.isLiftMotorAllowed()) {
                if(getGamepad1().right_bumper && !getGamepad1().left_bumper) {
                    LIFT.driveWithEncoder(50);
                }else if(!getGamepad1().right_bumper && getGamepad1().left_bumper){
                    LIFT.driveWithEncoder(-50);
                }else{
                    LIFT.driveWithEncoder(0);
                }
            }
        }else if(getGamepad2Functions().hasF2()) {
            if(DEVICES.isSpinnerMotorAllowed()) {
                SPINNER.driveWithEncoder((int) Range.clip((getGamepad1().left_trigger - getGamepad1().right_trigger) * 100, -100, 100));
            }
            if(DEVICES.isLiftMotorAllowed()) {
                if(getGamepad1().right_bumper && !getGamepad1().left_bumper) {
                    LIFT.driveWithEncoder(50);
                }else if(!getGamepad1().right_bumper && getGamepad1().left_bumper){
                    LIFT.driveWithEncoder(-50);
                }else{
                    LIFT.driveWithEncoder(0);
                }
            }
        }
    }

    @Override
    public void stop() {

    }

}