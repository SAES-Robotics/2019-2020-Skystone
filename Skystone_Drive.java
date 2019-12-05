package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;


@TeleOp(name="Skystone Drive Alex")

public class SkystoneDrive_alex extends LinearOpMode {
    private DcMotor fl, fr, bl, br, am,tp,en;
    private Servo ts,ms,dbs;


    public void runOpMode() {

        //map the motors
        fl = hardwareMap.get(DcMotor.class, "frontLeft");
        fr = hardwareMap.get(DcMotor.class, "frontRight");
        bl = hardwareMap.get(DcMotor.class, "backLeft");
        br = hardwareMap.get(DcMotor.class, "backRight");
        ts = hardwareMap.get(Servo.class, "topServo");
        ms = hardwareMap.get(Servo.class, "middleServo");
        dbs = hardwareMap.get(Servo.class, "doubleServo");
        am = hardwareMap.get(DcMotor.class, "liftingMotor");
        tp = hardwareMap.get(DcMotor.class, "topMotor");
        en = hardwareMap.get(DcMotor.class, "endMotor");
        /*robotlift = hardwareMap.get(DcMotor.class, "RoboLift");
        armrotate = hardwareMap.get(DcMotor.class, "ArmRotate");
        armextend = hardwareMap.get(DcMotor.class, "ArmExtend");

        intake = hardwareMap.get(CRServo.class, "IntakeServo");
        */
        //make sure everything brakes
        fl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        fr.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        bl.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        br.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        //reverses the motors to make the logic easier
        fr.setDirection(DcMotorSimple.Direction.REVERSE);
        br.setDirection(DcMotorSimple.Direction.REVERSE);


        //encoder settings
        /*robotlift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armextend.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        armextend.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        armrotate.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        robotlift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);*/


        //update status
        telemetry.addData("Status", "Initialized");
        telemetry.update();


        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        boolean d_pad = true;
        double speed = 0.5;
        double slow_speed = 0.1;
        double speed_mutplier = 0.3;
        boolean down = false;

        double speed_mutplier_2 = 0.1;
        double ms_speed = 0;
        double dbs_speed = 0;
        double ts_speed = 0;

        while (opModeIsActive()) {
            //telemetry.addData("hi", opModeIsActive());
            /* gamepad2 controls */
            if(this.gamepad2.a ){
                if(dbs_speed + speed_mutplier_2 >= 0.5){
                    dbs_speed = 0.5;
                }
                else{
                dbs_speed += speed_mutplier_2;
                }
                dbs.setPosition(dbs_speed);
            }
            else if(this.gamepad2.b ){
                if(dbs_speed - speed_mutplier_2 <= 0){
                    dbs_speed = 0;
                }
                else {
                dbs_speed -=  speed_mutplier_2;
                }
                dbs.setPosition(dbs_speed);
            }

            telemetry.addData("dbs speed",dbs_speed);



            if(this.gamepad2.left_trigger>0.3 ){
                if(ts_speed + speed_mutplier_2 >= 1){
                    ts_speed = 1;
                }
                else {
                ts_speed += speed_mutplier_2;}
                ts.setPosition(ts_speed);
            }
            else if(this.gamepad2.left_bumper){
                if(ts_speed - speed_mutplier_2 <= 0){
                    ts_speed = 0;
                }
                else{
                    ts_speed -= speed_mutplier_2;
                }
                ts.setPosition(ts_speed);
            }

            telemetry.addData("ts speed",ts_speed);

            if(this.gamepad2.right_trigger > 0.3){
                if(ms_speed + speed_mutplier_2 >= 1){
                    ms_speed = 1;
                }
                else{
                ms_speed += speed_mutplier_2;
                }
                ms.setPosition(ms_speed);
            }
            else if(this.gamepad2.right_bumper){
                if(ms_speed - speed_mutplier_2 <= 0){
                    ms_speed = 0;
                }
                else{
                ms_speed -= speed_mutplier_2;
                }
                ms.setPosition(ms_speed);
                }



            telemetry.addData("ms speed",ms_speed);

            if(gamepad2.dpad_up){
                speed_mutplier_2 += 0.05;
            }
            if(gamepad2.dpad_up){
                speed_mutplier_2 -= 0.05;
            }
            en.setPower(this.gamepad2.left_stick_y * 5);
            en.setPower(this.gamepad2.right_stick_y * 5);
            tp.setPower(this.gamepad2.left_stick_x * 5);
            /* gamepad1 controls */
            //sets the speed to slow if its down

            if(gamepad1.left_trigger > 0.5){
                speed /= 3;
                down = true;

            }

            //sets the speed modifier
            if(gamepad1.dpad_up){
                if(d_pad && speed < 1.0)
                    speed += speed_mutplier;
                slow_speed += speed_mutplier;
                d_pad = false;
            }
            else if(gamepad1.dpad_down){
                if(d_pad && speed > speed_mutplier)
                    if(speed == slow_speed){
                        slow_speed -= speed_mutplier;
                    }
                    else{
                        speed = speed - speed_mutplier;
                    }
                d_pad = false;
            }else
                d_pad = true;



            double move0 = this.gamepad1.left_stick_y - this.gamepad1.left_stick_x;
            double move1 = this.gamepad1.left_stick_y + this.gamepad1.left_stick_x;

            //run the wheels
            fl.setPower( (move1 - this.gamepad1.right_stick_x) * speed );
            fr.setPower( (move0 + this.gamepad1.right_stick_x) * speed );
            bl.setPower( (move0 - this.gamepad1.right_stick_x) * speed );
            br.setPower( (move1 + this.gamepad1.right_stick_x) * speed );

            if(down){
                speed *= 2;
                down = false;

            }
            telemetry.addData("brspeed",((move1 + this.gamepad1.right_stick_x) * speed));
            telemetry.addData("flspeed",((move1 - this.gamepad1.right_stick_x) * speed));
            telemetry.addData("blspeed",((move0 - this.gamepad1.right_stick_x) * speed));
            telemetry.addData("frspeed",((move0 + this.gamepad1.right_stick_x) * speed));
            telemetry.update();
        }

    }
}
