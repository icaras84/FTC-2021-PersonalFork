/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;

@Config
@Autonomous(name = "NewAuto", group = "Test")
public class NewAuto extends OpMode {
    // Hardware
    private DcMotor leftDrive = null;
    private DcMotor rightDrive = null;
    private DcMotor duckSpinner = null;
    private DcMotor depBelt = null;
    private Servo depLow = null;
    private Servo depMid = null;
    private Servo depTilt = null;
    private DcMotor collector = null;
    private Servo collectorArm = null;
    private Servo capstoneArm = null;
    private DistanceSensor distanceLeft = null;
    private DistanceSensor distanceRight = null;

    // Consts
    private static float DRIVE_POWER = 0.375f;
    private static double TICKS_PER_INCH = 43.24;
    private static double TURN_RATIO = 7.98;
    private static double ANGLE_CONST = 1.23;
    private static double DUCK_POWER = 0.0;
    private static double DEP_BELT_POWER = 0.9;
    private static double DEP_UP = 0.6;
    private static double DEP_DOWN = 0.44;
    private static double LOW_OPEN = 0.98;
    private static double LOW_CLOSE = 0.56;
    private static double MID_OPEN = 0.9;
    private static double MID_CLOSE = 0.48;
    private static double COLLECTOR_UP = 0.96;
    private static double COLLECTOR_DOWN = 0.23;
    private static double COLLECTOR_POWER = -1;
    private static double timerRatio = 0.0;
    private static double duckPowerMin = 0.2;  // min duck spinner speed (0 - 1.0)
    private static double duckPowerMax = 0.45;  // max duck spinner speed (0 - 1.0)
    private static double duckRampTime = 1.25;  // duck spinner ramp time (seconds, >0)
    private static double CAP_IN = 0;
    private static double CAP_MID = 0.52;
    private static double CAP_UP = 0.35;
    private static double CAP_DOWN = 0.87;

    // Members
    private ElapsedTime runtime = new ElapsedTime();

    // Members
    private boolean done = false;
    private boolean driveCmdRunning = false;
    private int autoStep = 0;
    private ButtonHandler buttons;
    private boolean redAlliance = false;
    private boolean duckSide = false;

    @Override
    public void init() {
        boolean error = false;
        telemetry.addData("Status", "Initializing...");

        // Drive Motors
        try {
            leftDrive = hardwareMap.get(DcMotor.class, "BL");
            rightDrive = hardwareMap.get(DcMotor.class, "BR");
            leftDrive.setDirection(DcMotor.Direction.FORWARD);
            rightDrive.setDirection(DcMotor.Direction.FORWARD);
        } catch (Exception e) {
            telemetry.log().add("Could not find drive");
            error = true;
        }

        // Duck Spinner
        try {
            duckSpinner = hardwareMap.get(DcMotor.class, "duck");
        } catch (Exception e) {
            telemetry.log().add("Could not find duck spinner");
            error = true;
        }

        // Depositor
        try {
            depBelt = hardwareMap.get(DcMotor.class, "Depbelt");
            depLow = hardwareMap.get(Servo.class, "Deplow");
            depMid = hardwareMap.get(Servo.class, "Depmid");
            depTilt = hardwareMap.get(Servo.class, "Deptilt");
        } catch (Exception e) {
            telemetry.log().add("Could not find depositor");
            error = true;
        }

        // Collector
        try {
            collector = hardwareMap.get(DcMotor.class, "Collector");
            collectorArm = hardwareMap.get(Servo.class, "CollectorArm");
        } catch (Exception e) {
            telemetry.log().add("Could not find collector");
            error = true;
        }

        // Capstone Grabber
        try {
            //capstoneHook = hardwareMap.get(Servo.class, "Caphook");
            capstoneArm = hardwareMap.get(Servo.class, "Caparm");
        } catch (Exception e) {
            telemetry.log().add("Could not find capstone dep");
            error = true;
        }

        // Distance Sensors
        try {
            distanceLeft = hardwareMap.get(DistanceSensor.class, "DL");
            distanceRight = hardwareMap.get(DistanceSensor.class, "DR");
        } catch (Exception e) {
            telemetry.log().add("Could not find distance sensors");
            error = true;
        }

        // Register buttons
        buttons = new ButtonHandler(this);
        buttons.register("RED", gamepad1, PAD_BUTTON.dpad_left);
        buttons.register("BLUE", gamepad1, PAD_BUTTON.dpad_right);
        buttons.register("DUCK", gamepad1, PAD_BUTTON.dpad_up);
        buttons.register("WAREHOUSE", gamepad1, PAD_BUTTON.dpad_down);

        // Initialization status
        String status = "Ready";
        if (error) {
            status = "Hardware Error";
        }
        depTilt.setPosition(DEP_UP);
        depLow.setPosition(LOW_CLOSE);
        depMid.setPosition(MID_CLOSE);
        capstoneArm.setPosition(CAP_IN);
        collectorArm.setPosition(COLLECTOR_UP);
        telemetry.addData("Status", status);
    }

    @Override
    public void init_loop() {
        if (buttons.get("RED")) redAlliance = true;
        if (buttons.get("BLUE")) redAlliance = false;
        if (buttons.get("DUCK")) duckSide = true;
        if (buttons.get("WAREHOUSE")) duckSide = false;
        telemetry.addData("Alliance", redAlliance ? "Red" : "Blue");
        telemetry.addData("Direction", duckSide ? "Duck" : "Warehouse");
    }

    @Override
    public void start() {
        driveStop();
        done = false;
        autoStep = 0;
        depTilt.setPosition(DEP_DOWN);
        depLow.setPosition(LOW_CLOSE);
        depMid.setPosition(MID_CLOSE);
        capstoneArm.setPosition(CAP_UP);
        collectorArm.setPosition(COLLECTOR_UP);
    }

    @Override
    public void loop() {
        // Stop when the autoSteps are complete
        if (done) {
            requestOpModeStop();
            return;
        }

        // Feedback
        telemetry.addData("Drive", "L %.2f/%d, R %.2f/%d",
                leftDrive.getPower(), leftDrive.getCurrentPosition(),
                rightDrive.getPower(), rightDrive.getCurrentPosition());

        // Don't start new commands until the last one is complete
        if (driveCmdRunning) {
            // When the motors are done
            if (!isBusy()) {
                // Clear the running flag
                driveCmdRunning = false;
                // Advance to the next autoStep
                autoStep++;
            }
            // Continue from the top of loop()
            return;
        }

        //
        // Code past here is not run when driveCmdRunning is true
        //

        // Step through the auto commands
        switch (autoStep) {
            /*case -2:
                sensorTimer.reset();
                leftSensorAccum = 0;
                autoStep++;
                break;
            case -1 :
                leftSensorAccum = (leftSensorAccum * 0.9) + (0.1 * distanceLeft.getDistance());
                if (sensorTimer.seconds() > SENSOR_TIME) {
                    autoStep++;
                }
                break;*/
            // Forward 16
            case 0:
                //driveTo(DRIVE_POWER, 16);
                driveTo(DRIVE_POWER, 24.8f);
                break;
            // Counter-clockwise 90
            case 1:
                //turnTo(DRIVE_POWER, -90);
                turnTo(DRIVE_POWER, 90);
                break;
            // Backwards 32
            case 2:
                //driveTo(-DRIVE_POWER * 0.4f, -36);
                driveTo(-DRIVE_POWER, -25);
                break;
            /* // Backward 35
            case 3:
                driveTo(DRIVE_POWER, 35);
                break;
            // Forward 3
            case 4:
                driveTo(DRIVE_POWER, 3);
                break;
            // Clockwise 45
            case 5:
                turnTo(DRIVE_POWER, 45);
                break;
            // Forward 31
            case 6:
                driveTo(DRIVE_POWER, 31);
                break; */
            // If we get past the end stop and end the loop
            default:
                driveStop();
                done = true;
                break;
        }
    }

    @Override
    public void stop() {
        // Reset to the standard drive mode
        leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void driveStop() {
        // Stop, zero the drive encoders, and enable RUN_TO_POSITION
        leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftDrive.setTargetPosition(leftDrive.getTargetPosition());
        leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftDrive.setPower(0);

        rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDrive.setTargetPosition(rightDrive.getTargetPosition());
        rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightDrive.setPower(0);
    }

    public boolean isBusy() {
        return leftDrive.isBusy() || rightDrive.isBusy();
    }

    public void driveTo(float speed, float distance) {
        // Don't allow new moves if we're still busy
        if (isBusy()) {
            telemetry.log().add("driveTo(): Motors in use");
            return;
        }

        // Set a target, translated from inches to encoder ticks
        int leftTarget = leftDrive.getCurrentPosition();
        int rightTarget = rightDrive.getCurrentPosition();
        leftTarget += distance * TICKS_PER_INCH;
        rightTarget += distance * TICKS_PER_INCH;
        leftDrive.setTargetPosition(leftTarget);
        rightDrive.setTargetPosition(rightTarget);

        // Start the motors
        driveCmdRunning = true;
        leftDrive.setPower(speed);
        rightDrive.setPower(speed);
    }

    public void turnTo(float speed, int angle) {
        // Don't allow new moves if we're still busy
        if (isBusy()) {
            telemetry.log().add("driveTo(): Motors in use");
            return;
        }

        // Fake turns using a distance translation
        // We have a gyro but let's start with just one control mode
        int leftTarget = leftDrive.getCurrentPosition();
        int rightTarget = rightDrive.getCurrentPosition();
        leftTarget += angle * TURN_RATIO;
        rightTarget -= angle * TURN_RATIO;
        leftDrive.setTargetPosition(leftTarget);
        rightDrive.setTargetPosition(rightTarget);

        // Start the motors
        driveCmdRunning = true;
        leftDrive.setPower(speed);
        rightDrive.setPower(-speed);
    }
}