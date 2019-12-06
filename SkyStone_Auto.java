package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

@Autonomous(name="SkyStone Autonomous")
public class SkyStone_Auto extends LinearOpMode {
    // motors
    private DcMotor fl, fr, bl, br, ta, da, md;

    //the gyroscope
    private BNO055IMU imu;

    //keep track of angles
    private Orientation lastAngles = new Orientation();
    private double globalAngle, correction;
    
    private static final double STRAFE = 44.1, MOVE = 35, TURN = 8; //taken from last years have to set


    /* MOVEMENT METHODS */

    // moves foward or backward
    private void moveTo(double cen, double speed, double angle) {
        fl.setTargetPosition( (int) Math.round(cen * MOVE) + fl.getCurrentPosition() );

        if (fl.getCurrentPosition() < fl.getTargetPosition())
            while ( opModeIsActive() && fl.getCurrentPosition() < fl.getTargetPosition() ) {
                correction = checkDirection(angle, 0.03);

                fl.setPower(-speed + correction);
                fr.setPower(-speed);
                bl.setPower(-speed + correction);
                br.setPower(-speed);
                idle();
            }
        else
            while ( opModeIsActive() && fl.getCurrentPosition() > fl.getTargetPosition() ) {
                correction = checkDirection(angle, 0.03);

                fl.setPower(speed + correction);
                fr.setPower(speed);
                bl.setPower(speed + correction);
                br.setPower(speed);
                idle();
            }

        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);
    }

    // turns a certain number of degrees
    private void turnTo(double degrees, double speed) {
        fl.setTargetPosition( (int) Math.round(degrees * TURN) + fl.getCurrentPosition() );
        double target = -getAngle() + degrees;

        if (degrees > 0) {   // turn right.
            fl.setPower(-speed);
            fr.setPower(speed);
            bl.setPower(-speed);
            br.setPower(speed);
        } else if (degrees < 0) {   // turn left.
            fl.setPower(speed);
            fr.setPower(-speed);
            bl.setPower(speed);
            br.setPower(-speed);
        } else
            return;

        int i = 1;
        if (fl.getCurrentPosition() < fl.getTargetPosition())
            while ( opModeIsActive() && fl.getCurrentPosition() < fl.getTargetPosition() ) { if(i++ % 15 == 0) getAngle(); else idle(); }
        else
            while ( opModeIsActive() && fl.getCurrentPosition() > fl.getTargetPosition() ) { if(i++ % 15 == 0) getAngle(); else idle(); }

        // turn the motors off.
        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);

        //correct the turn until within acceptable error bounds
        double error = target + getAngle();

        if(Math.abs(error) > 2)
            turnTo(error, pow);
    }

    // strafes a certain number of centimeters (not recommended)
    private void strafeTo(double cen, double pow, double angle) {
        fl.setTargetPosition( (int) Math.round(cent * STRAFE) + fl.getCurrentPosition() );

        if (fl.getCurrentPosition() < fl.getTargetPosition())
            while ( opModeIsActive() && fl.getCurrentPosition() < fl.getTargetPosition() ) {
                correction = checkDirection(angle, 0.02);

                fl.setPower(-pow + correction);
                fr.setPower(pow - correction);
                bl.setPower(pow);
                br.setPower(-pow);
            }
        else
            while ( opModeIsActive() && fl.getCurrentPosition() > fl.getTargetPosition() ) {
                correction = checkDirection(angle, 0.02);

                fl.setPower(pow + correction);
                fr.setPower(-pow - correction);
                bl.setPower(-pow);
                br.setPower(pow);
            }

        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);
    }


    /* COORDINATION METHODS */

    /**
     * Resets the cumulative angle tracking to zero.
     */
    private void resetAngle() {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }

    /**
     * Get current cumulative angle rotation from last reset.
     * @return Angle in degrees. + = left, - = right.
     */
    private double getAngle() {
        // We experimentally determined the Z axis is the axis we want to use for heading angle.
        // We have to process the angle because the imu works in euler angles so the Z axis is
        // returned as 0 to +180 or 0 to -180 rolling back to -179 or +179 when rotation passes
        // 180 degrees. We detect this transition and track the total cumulative angle of rotation.

        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    /**
     * See if we are moving in a straight line and if not return a power correction value.
     * @return Power adjustment, + is adjust left - is adjust right.
     */
    private double checkDirection(double a, double gain) {
        // The gain value determines how sensitive the correction is to direction changes.
        // You will have to experiment with your robot to get small smooth direction changes
        // to stay on a straight line.
        double correction, angle;

        angle = getAngle() - a;

        if (angle == 0)
            correction = 0;             // no adjustment.
        else
            correction = -angle * gain;        // reverse sign of angle for correction.

        return correction;
    }


    /* OP MODE CODE */

    public void runOpMode() {

        //map the motors
        fl = hardwareMap.get(DcMotor.class, "frontLeft");
        fr = hardwareMap.get(DcMotor.class, "frontRight");
        bl = hardwareMap.get(DcMotor.class, "backLeft");
        br = hardwareMap.get(DcMotor.class, "backRight");
        ta = hardwareMap.get(DcMotor.class, "topArm");
        da = hardwareMap.get(DcMotor.class, "downArm");
        md = hardwareMap.get(DcMotor.class, "middleArm");

        //make sure everything brakes
        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        ta.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        da.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //reverses the motors to make the logic easier
        fr.setDirection(DcMotorSimple.Direction.REVERSE);
        br.setDirection(DcMotorSimple.Direction.REVERSE);

        //encoder settings
        ta.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        da.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        md.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        ta.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        da.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        md.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //update status
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        //TODO

    }
}
