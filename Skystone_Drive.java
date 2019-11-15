package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;


@TeleOp(name="Skystone Drive")

public class SkystoneDrive extends LinearOpMode {
    private DcMotor fl, fr, bl, br, ta,da,md;


    public void runOpMode() {

        //map the motors
        fl = hardwareMap.get(DcMotor.class, "frontLeft");
        fr = hardwareMap.get(DcMotor.class, "frontRight");
        bl = hardwareMap.get(DcMotor.class, "backLeft");
        br = hardwareMap.get(DcMotor.class, "backRight");
        ta = hardwareMap.get(DcMotor.class, "topArm");
        da = hardwareMap.get(DcMotor.class, "downArm");
        md = hardwareMap.get(DcMotor.class, "middleArm");
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
        ta.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); //float
        da.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //reverses the motors to make the logic easier
        fr.setDirection(DcMotorSimple.Direction.REVERSE);
        br.setDirection(DcMotorSimple.Direction.REVERSE);

        ta.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        da.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ta.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        da.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        md.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        md.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
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
        double arm_speed = 0.5;
        while (opModeIsActive()) {
            //telemetry.addData("hi", opModeIsActive());
            /* gamepad2 controls */
            if(this.gamepad2.dpad_right){
                ta.setPower(0.5);
            }
            else if(this.gamepad2.dpad_left){
                ta.setPower(-0.15);
            }
            else{ta.setPower(0);}


            if(this.gamepad2.dpad_up){
                da.setPower(-arm_speed);
            }
            else if(this.gamepad2.dpad_down){
                da.setPower(arm_speed);}

            else{da.setPower(0);}

            if(this.gamepad2.left_trigger > 0.3){
                md.setPower(arm_speed);
            }
            else if(this.gamepad2.right_trigger > 0.3){
                md.setPower(-arm_speed);}

            else{md.setPower(0);}


            telemetry.addData("encoder", ta.getCurrentPosition());
            telemetry.addData("encoder2", da.getCurrentPosition());
            telemetry.addData("encoder3", md.getCurrentPosition());
            //runs the intake servo
            /*if(this.gamepad2.x)
                intake.setPower(1);
            else if(this.gamepad2.a)
                intake.setPower(0);
            else if(this.gamepad2.b)
                intake.setPower(-1);

            //controls for the robot lift
            robotlift.setPower( (this.gamepad2.left_bumper && (robotlift.getCurrentPosition() > 0 || this.gamepad2.y) ? 1 : 0) - (this.gamepad2.right_bumper ? 1 : 0) );

            //controls for the arm
            armrotate.setPower( (this.gamepad2.left_trigger - this.gamepad2.right_trigger) * 0.4 ); //positive is up/counterclockwise
            armextend.setPower( ( (this.gamepad2.dpad_down ? 1 : 0) - ( this.gamepad2.dpad_up ? 1 : 0) ) * 0.8 );
            */
            /* gamepad1 controls */
            //sets the speed to slow if its down

            if(gamepad1.left_trigger > 0.5){
                speed /= 2;
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





            double move0 = this.gamepad1.left_stick_y + this.gamepad1.left_stick_x;
            double move1 = this.gamepad1.left_stick_y - this.gamepad1.left_stick_x;

            //run the wheels
            fl.setPower( (move1 - this.gamepad1.right_stick_x) * speed );
            fr.setPower( (move0 + this.gamepad1.right_stick_x) * speed );
            bl.setPower( (move0 - this.gamepad1.right_stick_x) * speed );
            br.setPower( (move1 + this.gamepad1.right_stick_x) * speed );

            if(down){
                speed *= 2;
                down = false;

            }
            telemetry.update();
        }

    }
}
