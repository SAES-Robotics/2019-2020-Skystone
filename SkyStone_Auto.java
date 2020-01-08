package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;


@Autonomous(name="SkyStone Autonomous")
public class SkyStone_Auto extends LinearOpMode {
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
    private static final boolean PHONE_IS_PORTRAIT = false;


    private static final String VUFORIA_KEY = "AfjKWRL/////AAABmfOEbMH3xUPLtlD2h7I4sNdQnVvF1uqK/Rye/iqJN0YFnthrt0LwwqpR6HnB5dY35Zgbdrn6BLpSjLWVrUPqQfsRh9BdK7rUVtf9yPhfSefN3OsepEiUEXxzB2rMxgUjs47BLgc3V7RCUwdgKGcWvY2k9xfWj+ampWjN1KaHLsB25KlgrF2IkTZyC0QuTk+Mbl0mu12iKhKv0EQsLS3WMya1qDD4KzwyH8mqEyqMg50WVYVT7rGdBk29nhgbe5TWhrqpVzSZdXdiP2Zqgg0C670HDC5LfOtkyatHetKYOSXq6n1r9xm4B9HjX97ZKy+vJMvZLp5EFvLLpz54O64+c1QJ8q4eRkHRb5e2NOXKjIgd  ";

    // Since ImageTarget trackables use mm to specifiy their dimensions, we must use mm for all the physical dimension.
    // We will define some constants and conversions here
    private static final float mmPerInch        = 25.4f;
    private static final float mmTargetHeight   = (6) * mmPerInch;          // the height of the center of the target image above the floor

    // Constant for Stone Target
    private static final float stoneZ = 2.00f * mmPerInch;

    // Constants for the center support targets
    private static final float bridgeZ = 6.42f * mmPerInch;
    private static final float bridgeY = 23 * mmPerInch;
    private static final float bridgeX = 5.18f * mmPerInch;
    private static final float bridgeRotY = 59;                                 // Units are degrees
    private static final float bridgeRotZ = 180;

    // Constants for perimeter targets
    private static final float halfField = 72 * mmPerInch;
    private static final float quadField  = 36 * mmPerInch;

    // Class Members
    private OpenGLMatrix lastLocation = null;
    private VuforiaLocalizer vuforia = null;
    boolean strafed = false;
    private boolean targetVisible = false;
    private float phoneXRotate    = 0;
    private float phoneYRotate    = 0;
    private float phoneZRotate    = 0;


    /* MOVEMENT METHODS */

    // moves foward or backward
    private void moveTo(double cen, double speed) {
        fr.setTargetPosition( (int) -Math.round(cen * MOVE) + fr.getCurrentPosition() );

        if (fr.getCurrentPosition() > fr.getTargetPosition())
            while ( opModeIsActive() && fr.getCurrentPosition() > fr.getTargetPosition() ) {
                //correction = checkDirection(cAngle, 0.03);
                telemetry.addData("forward, encoder",fr.getCurrentPosition());
                fl.setPower(speed - correction);
                fr.setPower(speed);
                bl.setPower(speed - correction);
                br.setPower(speed);
                idle();
                telemetry.update();
            }
        else
            while ( opModeIsActive() && fr.getCurrentPosition() < fr.getTargetPosition() ) {
                //correction = checkDirection(cAngle, 0.03);
                telemetry.addData("backward, encoder",fr.getCurrentPosition());
                fl.setPower(-speed + correction);
                fr.setPower(-speed);
                bl.setPower(-speed + correction);
                br.setPower(-speed);
                idle();
                telemetry.update();
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
        double target = getAngle() - degrees;

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
        double error = -target + getAngle();

        if(error * error > 2)
            turnToo(error, speed);
    }

    // strafes a certain number of centimeters (not recommended)
    private void strafeTo(double cen, double pow) {
        fr.setTargetPosition( (int) -Math.round(cen * STRAFE) + fr.getCurrentPosition());
        telemetry.addData("Status", fr.getTargetPosition());
        telemetry.addData("Status", fr.getCurrentPosition());
        telemetry.update();
        if (fr.getCurrentPosition() < fr.getTargetPosition())
            while ( opModeIsActive() && fr.getCurrentPosition() < fr.getTargetPosition() ) {
                //correction = checkDirection(cAngle, 0.001);

                fl.setPower(-0.5 );
                fr.setPower(0.5);
                bl.setPower(0.5);
                br.setPower(-0.5);
            }
        else
            while ( opModeIsActive() && fr.getCurrentPosition() > fr.getTargetPosition() ) {
                //correction = checkDirection(cAngle, 0.001);


                fl.setPower(0.5);
                fr.setPower(-0.5);
                bl.setPower(-0.5);
                br.setPower(0.5);
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


    /**
     * Code for the detection code
     */
    private VuforiaTrackables detection() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CAMERA_CHOICE;
        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
        // Load the data sets for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        VuforiaTrackables targetsSkyStone = this.vuforia.loadTrackablesFromAsset("Skystone");

        // Load the data sets for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        VuforiaTrackable stoneTarget = targetsSkyStone.get(0);
        stoneTarget.setName("Stone Target");
        VuforiaTrackable blueRearBridge = targetsSkyStone.get(1);
        blueRearBridge.setName("Blue Rear Bridge");
        VuforiaTrackable redRearBridge = targetsSkyStone.get(2);
        redRearBridge.setName("Red Rear Bridge");
        VuforiaTrackable redFrontBridge = targetsSkyStone.get(3);
        redFrontBridge.setName("Red Front Bridge");
        VuforiaTrackable blueFrontBridge = targetsSkyStone.get(4);
        blueFrontBridge.setName("Blue Front Bridge");
        VuforiaTrackable red1 = targetsSkyStone.get(5);
        red1.setName("Red Perimeter 1");
        VuforiaTrackable red2 = targetsSkyStone.get(6);
        red2.setName("Red Perimeter 2");
        VuforiaTrackable front1 = targetsSkyStone.get(7);
        front1.setName("Front Perimeter 1");
        VuforiaTrackable front2 = targetsSkyStone.get(8);
        front2.setName("Front Perimeter 2");
        VuforiaTrackable blue1 = targetsSkyStone.get(9);
        blue1.setName("Blue Perimeter 1");
        VuforiaTrackable blue2 = targetsSkyStone.get(10);
        blue2.setName("Blue Perimeter 2");
        VuforiaTrackable rear1 = targetsSkyStone.get(11);
        rear1.setName("Rear Perimeter 1");
        VuforiaTrackable rear2 = targetsSkyStone.get(12);
        rear2.setName("Rear Perimeter 2");
        // For convenience, gather together all the trackable objects in one easily-iterable collection */
        List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(targetsSkyStone);

        // Set the position of the Stone Target.  Since it's not fixed in position, assume it's at the field origin.
        // Rotated it to to face forward, and raised it to sit on the ground correctly.
        // This can be used for generic target-centric approach algorithms
        stoneTarget.setLocation(OpenGLMatrix
                .translation(0, 0, stoneZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        //Set the position of the bridge support targets with relation to origin (center of field)
        blueFrontBridge.setLocation(OpenGLMatrix
                .translation(-bridgeX, bridgeY, bridgeZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 0, bridgeRotY, bridgeRotZ)));

        blueRearBridge.setLocation(OpenGLMatrix
                .translation(-bridgeX, bridgeY, bridgeZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 0, -bridgeRotY, bridgeRotZ)));

        redFrontBridge.setLocation(OpenGLMatrix
                .translation(-bridgeX, -bridgeY, bridgeZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 0, -bridgeRotY, 0)));

        redRearBridge.setLocation(OpenGLMatrix
                .translation(bridgeX, -bridgeY, bridgeZ)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 0, bridgeRotY, 0)));

        //Set the position of the perimeter targets with relation to origin (center of field)
        red1.setLocation(OpenGLMatrix
                .translation(quadField, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        red2.setLocation(OpenGLMatrix
                .translation(-quadField, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        front1.setLocation(OpenGLMatrix
                .translation(-halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

        front2.setLocation(OpenGLMatrix
                .translation(-halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 90)));

        blue1.setLocation(OpenGLMatrix
                .translation(-quadField, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));

        blue2.setLocation(OpenGLMatrix
                .translation(quadField, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));

        rear1.setLocation(OpenGLMatrix
                .translation(halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        rear2.setLocation(OpenGLMatrix
                .translation(halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        // Create a transformation matrix describing where the phone is on the robot.
        // Info:  The coordinate frame for the robot looks the same as the field.
        // The robot's "forward" direction is facing out along X axis, with the LEFT side facing out along the Y axis.
        // Z is UP on the robot.  This equates to a bearing angle of Zero degrees.
        // The phone starts out lying flat, with the screen facing Up and with the physical top of the phone
        // pointing to the LEFT side of the Robot.
        // The two examples below assume that the camera is facing forward out the front of the robot.

        // We need to rotate the camera around it's long axis to bring the correct camera forward.
        if (CAMERA_CHOICE == BACK) {
            phoneYRotate = -90;
        } else {
            phoneYRotate = 90;
        }

        // Rotate the phone vertical about the X axis if it's in portrait mode
        if (PHONE_IS_PORTRAIT) {
            phoneXRotate = 90;
        }

        // Next, translate the camera lens to where it is on the robot.
        // In this example, it is centered (left to right), but forward of the middle of the robot, and above ground level.
        final float CAMERA_FORWARD_DISPLACEMENT = 4.0f * mmPerInch;   // eg: Camera is 4 Inches in front of robot center
        final float CAMERA_VERTICAL_DISPLACEMENT = 8.0f * mmPerInch;   // eg: Camera is 8 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix robotFromCamera = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES, phoneYRotate, phoneZRotate, phoneXRotate));

        for (VuforiaTrackable trackable : allTrackables) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(robotFromCamera, CAMERA_CHOICE);
        }

        return targetsSkyStone;
    }


    private void pickup(){
        // move the arm up
        da.setTargetPosition(-2400);
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

        da.setTargetPosition(0);
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

        ta.setTargetPosition(0);
        ta.setPower(-0.25);
        while(ta.getCurrentPosition()>ta.getTargetPosition() && opModeIsActive()){
            telemetry.addData("ta",ta.getCurrentPosition());
            telemetry.update();
        }
        ta.setPower(0);
    }

    private void drop(){

        ta.setTargetPosition(750);
        ta.setPower(0.4);
        while(ta.getCurrentPosition()<ta.getTargetPosition() && opModeIsActive()){
            telemetry.addData("ta",ta.getCurrentPosition());
            telemetry.update();
        }
        ta.setPower(0);

        md.setTargetPosition(-1800);
        md.setPower(0.5);
        while (md.getCurrentPosition()>md.getTargetPosition() && opModeIsActive()){
            telemetry.addData("md",md.getCurrentPosition());
            telemetry.update();
        }
        md.setPower(0);


        strafeTo(-25,0.5);

        moveTo(-10,0.5);



        ta.setTargetPosition(0);
        ta.setPower(-0.25);
        while(ta.getCurrentPosition()>ta.getTargetPosition() && opModeIsActive()){
            telemetry.addData("ta",ta.getCurrentPosition());
            telemetry.update();
        }
        ta.setPower(0);

        md.setTargetPosition(0);
        md.setPower(-0.3);
        while (md.getCurrentPosition()<md.getTargetPosition() && opModeIsActive()){
            telemetry.addData("md",md.getCurrentPosition());
            telemetry.update();
        }
        md.setPower(0);

        da.setTargetPosition(0);
        da.setPower(-0.25);
        while(da.getCurrentPosition()<da.getTargetPosition() && opModeIsActive()){
            telemetry.addData("da",da.getCurrentPosition());
            telemetry.update();
        }
        da.setPower(0);



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

        VuforiaTrackables targetsSkyStone = detection();

        targetsSkyStone.activate();

        //update status
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        //update status
        telemetry.addData("Status", "Running2");
        telemetry.update();




        moveTo(60,1);



        targetVisible = false;

        double instrafed = 0;
        VuforiaTrackable target;
        while(!targetVisible && opModeIsActive()) {//!targetVisible && opModeIsActive()
            for (VuforiaTrackable trackable : targetsSkyStone) {
                if ((((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()) && trackable.getName() == "Stone Target") {
                    telemetry.addData("Visible Target", trackable.getName());
                    targetVisible = true;
                    target = trackable;

                    // getUpdatedRobotLocation() will return null if no new information is available since
                    // the last time that call was made, or if the trackable is not currently visible.
                    OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener) trackable.getListener()).getUpdatedRobotLocation();
                    if (robotLocationTransform != null) {
                        lastLocation = robotLocationTransform;
                    }
                    break;
                }
            }

            if (targetVisible) {
                // express position (translation) of robot in inches.
                VectorF translation = lastLocation.getTranslation();
                telemetry.addData("Pos (in)", "{X, Y, Z} = %.1f, %.1f, %.1f",
                        translation.get(0) / mmPerInch, translation.get(1) / mmPerInch, translation.get(2) / mmPerInch);

                // express the rotation of the robot in degrees.
                Orientation rotation = Orientation.getOrientation(lastLocation, EXTRINSIC, XYZ, DEGREES);
                telemetry.addData("Rot (deg)", "{Roll, Pitch, Heading} = %.0f, %.0f, %.0f", rotation.firstAngle, rotation.secondAngle, rotation.thirdAngle);
                telemetry.update();
            }
            else {

                if (instrafed > -70 && !strafed) {
                    strafeTo(-10, 1);
                    instrafed -= 10;
                } else {
                    strafed = true;
                    strafeTo(20, 1);
                    instrafed += 20;
                }

            }

        }


        telemetry.update();

        VectorF translation = lastLocation.getTranslation();
        telemetry.addData("Pos (in)", "{X, Y, Z} = %.1f, %.1f, %.1f",
                translation.get(0) / mmPerInch, translation.get(1) / mmPerInch, translation.get(2) / mmPerInch);
        strafeTo(((translation.get(1)/ mmPerInch)*2.54)+7,0.75);
        telemetry.addData("strafed","done");
        telemetry.update();
        instrafed -= (-(translation.get(0) / mmPerInch)*2.54) - 32;

        moveTo((-(translation.get(0) / mmPerInch)*2.54)-32,0.25);

        pickup();


        moveTo(-12,0.6);
        turnTo(-90,0.4);
        sv.setPosition(0.5);
        telemetry.addData("strafed",instrafed);
        telemetry.addData("moved",((-(translation.get(0) / mmPerInch)*2.54) - 32));
        telemetry.update();

        betterwait(4000);
        moveTo(173+instrafed,0.8);

        da.setTargetPosition(-1500);
        da.setPower(0.6);
        while(da.getCurrentPosition()>da.getTargetPosition() && opModeIsActive()){
            telemetry.addData("da",da.getCurrentPosition());
            telemetry.update();
        }
        da.setPower(0);

        strafeTo(72 ,0.4);

        drop();


        turnTo(180,0.5);
        strafeTo(-25,0.5);
        moveTo(-10,0.5);
        sv.setPosition(0);
        betterwait(250);

        moveTo(13,0.4);
        turnTo(-45,0.4);
        moveTo(-25,0.4);

        /*
        strafeTo(60,0.4);
        turnTo(-25,0.4);
        moveTo(12,0.4);
        strafeTo(60,0.4);
        turnTo(-25,0.4);
        moveTo(6,0.4);
        strafeTo(30,0.4);
        turnTo(-25,0.4);*/


        sv.setPosition(0.5);
        betterwait(500);

        strafeTo(30,1);
        moveTo(35,1);

        targetsSkyStone.deactivate();



    }

}