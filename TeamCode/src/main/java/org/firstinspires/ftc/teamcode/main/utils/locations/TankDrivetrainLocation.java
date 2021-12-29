package org.firstinspires.ftc.teamcode.main.utils.locations;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.R;
import org.firstinspires.ftc.teamcode.main.utils.interactions.InteractionSurface;
import org.firstinspires.ftc.teamcode.main.utils.interactions.groups.StandardTankVehicleDrivetrain;
import org.firstinspires.ftc.teamcode.main.utils.interactions.items.StandardMotor;

public class TankDrivetrainLocation extends Location {

    public enum Action {
        SET_SPEED,
        SET_VOLTAGE,
        MOVE_DISTANCE_IN_INCHES
    }

    private StandardTankVehicleDrivetrain DRIVETRAIN;

    public TankDrivetrainLocation(HardwareMap hardware) {
        try {
            StandardMotor rt = new StandardMotor(hardware, hardware.appContext.getString(R.string.DRIVETRAIN_RIGHT_TOP_DRIVING_MOTOR), DcMotorSimple.Direction.FORWARD, 28, 20, 1);
            StandardMotor rb = new StandardMotor(hardware, hardware.appContext.getString(R.string.DRIVETRAIN_RIGHT_BOTTOM_DRIVING_MOTOR), DcMotorSimple.Direction.FORWARD, 28, 20, 1);
            StandardMotor lt = new StandardMotor(hardware, hardware.appContext.getString(R.string.DRIVETRAIN_LEFT_TOP_DRIVING_MOTOR), DcMotorSimple.Direction.FORWARD, 28, 20, 1);
            StandardMotor lb = new StandardMotor(hardware, hardware.appContext.getString(R.string.DRIVETRAIN_LEFT_BOTTOM_DRIVING_MOTOR), DcMotorSimple.Direction.FORWARD, 28, 20, 1);
            DRIVETRAIN = new StandardTankVehicleDrivetrain(rt, rb, lt, lb);
        } catch(Exception ignored) {}
    }

    public void handleInput(Action action, int rightInput, int leftInput) {
        if(DRIVETRAIN == null) {
            return;
        }
        switch(action) {
            case SET_SPEED:
                DRIVETRAIN.driveWithEncoder(rightInput, -leftInput);
                break;
            case SET_VOLTAGE:
                DRIVETRAIN.driveWithoutEncoder(rightInput, -leftInput);
                break;
            case MOVE_DISTANCE_IN_INCHES:
                DRIVETRAIN.driveDistance(rightInput, -leftInput, 50);
                break;
        }
    }

    @Override
    public void stop() {
        if(DRIVETRAIN == null) {
            return;
        }
        DRIVETRAIN.stop();
    }

    @Override
    public boolean isInputLocation() {
        return true;
    }

    @Override
    public boolean isOutputLocation() {
        return false;
    }

    @Override
    public InteractionSurface getInternalInteractionSurface() {
        return DRIVETRAIN;
    }

}