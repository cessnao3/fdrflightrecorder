package com.ianorourke.fdrflightrecorder.Sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class GyroscopeReader implements SensorEventListener {
    private boolean initialized = false;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    private float x = 0.0f, y = 0.0f;
    private float modX = 0.0f, modY = 0.0f;

    private boolean shouldCalibrate = false;
    private boolean isReversed = false;

    public interface GyroscopeReaderInterface {
        public void receivedValues(float x, float y);
    }

    private GyroscopeReaderInterface gyroscopeReaderInterface;

    public GyroscopeReader(Context c) {
        sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Nothing
    }

    public final void resetCalibration() {
        modX = 0.0f;
        modY = 0.0f;

        isReversed = false;
        shouldCalibrate = false;
    }

    public final void calibrate() {
        modX = x;
        modY = y;
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float[] gravityVector = {event.values[0], event.values[1], event.values[2]};

        if (shouldCalibrate) {
            isReversed = gravityVector[2] < 0.0f;
        }

        if (isReversed) {
            x = (float) -Math.toDegrees(Math.atan2(-gravityVector[0], -gravityVector[2]));
            y = (float) -Math.toDegrees(Math.atan2(-gravityVector[1], -gravityVector[2]));
        } else {
            x = (float) -Math.toDegrees(Math.atan2(gravityVector[0], gravityVector[2]));
            y = (float) Math.toDegrees(Math.atan2(gravityVector[1], gravityVector[2]));
        }

        if (x < -180.0f) x += 360.0f;
        else if (x > 180.0f) x -= 360.0f;

        if (y < -180.0f) y += 360.0f;
        else if (x > 180.0f) x -= 360.0f;

        if (shouldCalibrate) {
            shouldCalibrate = false;
            calibrate();
        }

        x -= modX;
        y -= modY;

        if (gyroscopeReaderInterface != null) gyroscopeReaderInterface.receivedValues(x, y);
    }

    public void setEnabled(boolean b, boolean cal) {
        shouldCalibrate = cal;
        setEnabled(b);
    }

    public void setEnabled(boolean b) {
        if (b) {
            if (gyroscopeSensor != null) {
                if (initialized) return;

                sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
                initialized = true;
            } else {
                Log.v("FDR", "No sensor");
            }
        } else {
            if (initialized) {
                sensorManager.unregisterListener(this);
                initialized = false;
            }
        }
    }

    public void setInterface(GyroscopeReaderInterface i) {
        gyroscopeReaderInterface = i;
    }
}
