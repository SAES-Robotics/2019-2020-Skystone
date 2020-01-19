package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

@Autonomous(name="SkyStone detection run")
public class Skystone_detection_run extends LinearOpMode {
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;
    private VuforiaLocalizer vuforia = null;

    private static final String VUFORIA_KEY = "AfjKWRL/////AAABmfOEbMH3xUPLtlD2h7I4sNdQnVvF1uqK/Rye/iqJN0YFnthrt0LwwqpR6HnB5dY35Zgbdrn6BLpSjLWVrUPqQfsRh9BdK7rUVtf9yPhfSefN3OsepEiUEXxzB2rMxgUjs47BLgc3V7RCUwdgKGcWvY2k9xfWj+ampWjN1KaHLsB25KlgrF2IkTZyC0QuTk+Mbl0mu12iKhKv0EQsLS3WMya1qDD4KzwyH8mqEyqMg50WVYVT7rGdBk29nhgbe5TWhrqpVzSZdXdiP2Zqgg0C670HDC5LfOtkyatHetKYOSXq6n1r9xm4B9HjX97ZKy+vJMvZLp5EFvLLpz54O64+c1QJ8q4eRkHRb5e2NOXKjIgd  ";

    public void runOpMode() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CAMERA_CHOICE;
        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
        Skystone_detection sky = new Skystone_detection(vuforia);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        waitForStart();

        String position = sky.vuforiascan(true,true);
        telemetry.addData("guess",position);
        telemetry.update();

        while( opModeIsActive() ) {
            idle();
        }
    }
}