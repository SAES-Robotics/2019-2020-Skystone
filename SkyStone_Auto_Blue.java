
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;


@Autonomous(name="SkyStone Blue")
public class SkyStone_Auto_Blue extends LinearOpMode {
    // motors
    private DcMotor fl, fr, bl, br, ta, da, md;
    private Servo sv;
    //the gyroscope
    private BNO055IMU imu;

    //keep track of angles
    private Orientation lastAngles = new Orientation();
    private double globalAngle, correction, cAngle = 0;

    private static final double STRAFE = 27, MOVE = 19.16, TURN = 14.55; //taken from last years have to set


    /* Variables for the detection */
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;
    private VuforiaLocalizer vuforia = null;

    private static final String VUFORIA_KEY = "AfjKWRL/////AAABmfOEbMH3xUPLtlD2h7I4sNdQnVvF1uqK/Rye/iqJN0YFnthrt0LwwqpR6HnB5dY35Zgbdrn6BLpSjLWVrUPqQfsRh9BdK7rUVtf9yPhfSefN3OsepEiUEXxzB2rMxgUjs47BLgc3V7RCUwdgKGcWvY2k9xfWj+ampWjN1KaHLsB25KlgrF2IkTZyC0QuTk+Mbl0mu12iKhKv0EQsLS3WMya1qDD4KzwyH8mqEyqMg50WVYVT7rGdBk29nhgbe5TWhrqpVzSZdXdiP2Zqgg0C670HDC5LfOtkyatHetKYOSXq6n1r9xm4B9HjX97ZKy+vJMvZLp5EFvLLpz54O64+c1QJ8q4eRkHRb5e2NOXKjIgd  ";



    /* MOVEMENT METHODS */

    // moves foward or backward
    private void moveTo(double cen, double speed) {
        fr.setTargetPosition( (int) -Math.round(cen * MOVE) + fr.getCurrentPosition() );

        if (fr.getCurrentPosition() > fr.getTargetPosition())
            while ( opModeIsActive() && fr.getCurrentPosition() > fr.getTargetPosition() ) {
                //correction = checkDirection(cAngle, 0.03);
                fl.setPower(speed - correction);
                fr.setPower(speed);
                bl.setPower(speed - correction);
                br.setPower(speed);
                idle();
            }
        else
            while ( opModeIsActive() && fr.getCurrentPosition() < fr.getTargetPosition() ) {
                //correction = checkDirection(cAngle, 0.03);
                fl.setPower(-speed + correction);
                fr.setPower(-speed);
                bl.setPower(-speed + correction);
                br.setPower(-speed);
                idle();
            }

        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);

        betterwait(250);
    }

    private void moveTosv(double cen, double speed) {
        fr.setTargetPosition( (int) -Math.round(cen * MOVE) + fr.getCurrentPosition() );

        if (fr.getCurrentPosition() > fr.getTargetPosition())
            while ( opModeIsActive() && fr.getCurrentPosition() > fr.getTargetPosition() ) {
                //correction = checkDirection(cAngle, 0.03);
                fl.setPower(speed - correction);
                fr.setPower(speed);
                bl.setPower(speed - correction);
                br.setPower(speed);
                sv.setPosition(0);
                idle();
            }
        else
            while ( opModeIsActive() && fr.getCurrentPosition() < fr.getTargetPosition() ) {
                //correction = checkDirection(cAngle, 0.03);
                fl.setPower(-speed + correction);
                fr.setPower(-speed);
                bl.setPower(-speed + correction);
                br.setPower(-speed);
                sv.setPosition(0);
                idle();
            }

        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);

        betterwait(250);
    }
    // turns a certain number of degrees
    //
    // makes sure that the current angle is only
    // modified once
    private void turnTo(double degrees, double speed) {
        cAngle += degrees;
        turnToo(degrees, speed);
    }

    //DONT USE THIS ONE
    private void turnToo(double degrees, double speed) {
        fr.setTargetPosition( (int) -Math.round(degrees * TURN) + fr.getCurrentPosition() );

        if (degrees > 0) {   // turn right.
            fl.setPower(speed);
            fr.setPower(-speed);
            bl.setPower(speed);
            br.setPower(-speed);
        } else if (degrees < 0) {   // turn left.
            fl.setPower(-speed);
            fr.setPower(speed);
            bl.setPower(-speed);
            br.setPower(speed);
        } else
            return;

        int i = 1;
        if (fr.getCurrentPosition() > fr.getTargetPosition())
            while ( opModeIsActive() && fr.getCurrentPosition() > fr.getTargetPosition() ) { if(i++ % 15 == 0) getAngle(); else idle(); }
        else
            while ( opModeIsActive() && fr.getCurrentPosition() < fr.getTargetPosition() ) { if(i++ % 15 == 0) getAngle(); else idle(); }

        // turn the motors off.
        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);

        betterwait(250);

        //correct the turn until within acceptable error bounds
        double error = cAngle + getAngle();

        if(error > 2)
            turnToo(error, speed);
    }

    private void turnTosv(double degrees, double speed) {
        cAngle += degrees;
        turnToosv(degrees, speed);
    }

    //DONT USE THIS ONE
    private void turnToosv(double degrees, double speed) {
        fr.setTargetPosition( (int) -Math.round(degrees * TURN) + fr.getCurrentPosition() );

        if (degrees > 0) {   // turn right.
            fl.setPower(speed);
            fr.setPower(-speed);
            bl.setPower(speed);
            br.setPower(-speed);
        } else if (degrees < 0) {   // turn left.
            fl.setPower(-speed);
            fr.setPower(speed);
            bl.setPower(-speed);
            br.setPower(speed);
        } else
            return;

        int i = 1;
        if (fr.getCurrentPosition() > fr.getTargetPosition())
            while ( opModeIsActive() && fr.getCurrentPosition() > fr.getTargetPosition() ) { sv.setPosition(0);if(i++ % 15 == 0) getAngle(); else idle(); }
        else
            while ( opModeIsActive() && fr.getCurrentPosition() < fr.getTargetPosition() ) { sv.setPosition(0);if(i++ % 15 == 0) getAngle(); else idle(); }

        // turn the motors off.
        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);

        betterwait(250);

        //correct the turn until within acceptable error bounds
        double error = cAngle + getAngle();

        if(error > 2)
            turnToo(error, speed);
    }

    // strafes a certain number of centimeters (not recommended)
    private void strafeTo(double cen, double pow) {
        fr.setTargetPosition( (int) -Math.round(cen * STRAFE) + fr.getCurrentPosition());
        if (fr.getCurrentPosition() < fr.getTargetPosition())
            while ( opModeIsActive() && fr.getCurrentPosition() < fr.getTargetPosition() ) {
                correction = checkDirection(cAngle, 0.02);

                fl.setPower(-pow);
                fr.setPower(pow - correction);
                bl.setPower(pow);
                br.setPower(-pow + correction);
            }
        else
            while ( opModeIsActive() && fr.getCurrentPosition() > fr.getTargetPosition() ) {
                correction = checkDirection(cAngle, 0.02);


                fl.setPower(pow);
                fr.setPower(-pow - correction);
                bl.setPower(-pow);
                br.setPower(pow + correction);
            }

        fl.setPower(0);
        fr.setPower(0);
        bl.setPower(0);
        br.setPower(0);

        betterwait(250);
    }


    /* COORDINATION METHODS */

    /**
     * Resets the cumulative angle tracking to zero.
     */
    private void resetAngle() {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
        cAngle = 0;
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

    private void betterwait(long millis){
        long t = System.currentTimeMillis() + millis;

        while(t > System.currentTimeMillis() && opModeIsActive())
            idle();
    }

    private void pickup(){
        // move the arm up
        da.setTargetPosition(-1700);
        da.setPower(0.6);
        while(da.getCurrentPosition()>da.getTargetPosition() && opModeIsActive()){
            telemetry.addData("da",da.getCurrentPosition());
            telemetry.update();
        }
        da.setPower(0);

        ta.setTargetPosition(700);
        ta.setPower(0.4);
        while(ta.getCurrentPosition()<ta.getTargetPosition() && opModeIsActive()){
            telemetry.addData("ta",ta.getCurrentPosition());
            telemetry.update();
        }
        ta.setPower(0);


        md.setTargetPosition(-1500);
        md.setPower(0.5);
        while (md.getCurrentPosition()>md.getTargetPosition() && opModeIsActive()){
            telemetry.addData("md",md.getCurrentPosition());
            telemetry.update();
        }
        md.setPower(0);

        //grab

        da.setTargetPosition(500);
        da.setPower(-0.25);
        while(da.getCurrentPosition()<da.getTargetPosition() && opModeIsActive()){
            telemetry.addData("da",da.getCurrentPosition());
            telemetry.update();
        }
        da.setPower(0);

        md.setTargetPosition(-1000);
        md.setPower(-0.3);
        while (md.getCurrentPosition()<md.getTargetPosition() && opModeIsActive()){
            telemetry.addData("md",md.getCurrentPosition());
            telemetry.update();
        }
        md.setPower(0);

        ta.setTargetPosition(300);
        ta.setPower(-0.25);
        while(ta.getCurrentPosition()>ta.getTargetPosition() && opModeIsActive()){
            telemetry.addData("ta",ta.getCurrentPosition());
            telemetry.update();
        }
        ta.setPower(0);

        da.setTargetPosition(400);
        da.setPower(0.6);
        while(da.getCurrentPosition()>da.getTargetPosition() && opModeIsActive()){
            telemetry.addData("da",da.getCurrentPosition());
            telemetry.update();
        }
        da.setPower(0);
    }

    private void drop(){
        // move the arm up
        da.setTargetPosition(-1000);
        da.setPower(0.6);
        while(da.getCurrentPosition()>da.getTargetPosition() && opModeIsActive()){
            telemetry.addData("da",da.getCurrentPosition());
            telemetry.update();
        }
        da.setPower(0);

        moveTo(25, 0.7);

        ta.setTargetPosition(700);
        ta.setPower(0.4);
        while(ta.getCurrentPosition()<ta.getTargetPosition() && opModeIsActive()){
            telemetry.addData("ta",ta.getCurrentPosition());
            telemetry.update();
        }
        ta.setPower(0);


        md.setTargetPosition(-1500);
        md.setPower(0.5);
        while (md.getCurrentPosition()>md.getTargetPosition() && opModeIsActive()){
            telemetry.addData("md",md.getCurrentPosition());
            telemetry.update();
        }
        md.setPower(0);

        strafeTo(-30,1.0);
        moveTo(-7,0.7);



        md.setTargetPosition(-1000);
        md.setPower(-0.3);
        while (md.getCurrentPosition()<md.getTargetPosition() && opModeIsActive()){
            telemetry.addData("md",md.getCurrentPosition());
            telemetry.update();
        }
        md.setPower(0);
    }


    /* OP MODE CODE */

    public void runOpMode() {

        //settings for the IMU
        BNO055IMU.Parameters param = new BNO055IMU.Parameters();

        param.mode = BNO055IMU.SensorMode.IMU;
        param.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        param.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        param.loggingEnabled = false;

        //map the motors and IMU
        fl = hardwareMap.get(DcMotor.class, "frontLeft");
        fr = hardwareMap.get(DcMotor.class, "frontRight");
        bl = hardwareMap.get(DcMotor.class, "backLeft");
        br = hardwareMap.get(DcMotor.class, "backRight");
        ta = hardwareMap.get(DcMotor.class, "topArm");
        da = hardwareMap.get(DcMotor.class, "downArm");
        md = hardwareMap.get(DcMotor.class, "middleArm");
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        sv = hardwareMap.get(Servo.class, "servo");

        imu.initialize(param);

        //make sure everything brakes
        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        ta.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        da.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //reverses the motors to make the logic easier
        fl.setDirection(DcMotorSimple.Direction.REVERSE);
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        br.setDirection(DcMotorSimple.Direction.REVERSE);
        ta.setDirection(DcMotorSimple.Direction.REVERSE);

        //encoder settings
        ta.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        da.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        md.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        fr.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        ta.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        da.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        md.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fr.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CAMERA_CHOICE;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
        Skystone_detection sky = new Skystone_detection(vuforia);

        //update status
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        //update status
        telemetry.addData("Status", "Running");
        telemetry.update();

        String position = sky.vuforiascan(true,true);
        telemetry.addData("SkyStone Position",position);
        telemetry.update();

        moveTo(71,0.7);
        sv.setPosition(0.5);

        if( position == "LEFT" ) {
            // SkyStone is on the left
            strafeTo(-35, 0.7);
        } else if( position == "RIGHT" ) {
            // SkyStone is on the right
            strafeTo(6, 0.7);
        } else {
            // SkyStone is in the center
            strafeTo(-14, 0.7);
        }

        pickup();

        moveTo(-10, 0.5);

        if( position == "LEFT" ) {
            // SkyStone is on the left
            strafeTo(-180, 1.0);
        } else if( position == "RIGHT" ) {
            // SkyStone is on the right

            //TODO move to depositing position
        } else {
            // SkyStone is in the center
            //TODO move to depositing position
        }

        drop();

        turnTo(155, 0.6);
        da.setTargetPosition(0);
        da.setPower(-0.25);
        while(da.getCurrentPosition()<da.getTargetPosition() && opModeIsActive()){
            telemetry.addData("da",da.getCurrentPosition());
            telemetry.update();
        }
        da.setPower(0);
        moveTo(-3,0.6);
        sv.setPosition(0);
        telemetry.addData("cangle",cAngle);
        telemetry.update();
        betterwait(4000);

        moveTosv(8, 0.25);
        moveTosv(50,0.5);
        turnTosv(-135,0.5);
        sv.setPosition(0.5);
        strafeTo(-15,0.5);
        moveTo(10,0.5);

    }

}
