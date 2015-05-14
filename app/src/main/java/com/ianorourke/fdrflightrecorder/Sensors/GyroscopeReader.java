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

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float[] gravityVector = {event.values[0], event.values[1], event.values[2]};

        x = (float) -Math.toDegrees(Math.atan2(gravityVector[0], gravityVector[2]));
        y = (float) Math.toDegrees(Math.atan2(gravityVector[1], gravityVector[2]));

        if (gyroscopeReaderInterface != null) gyroscopeReaderInterface.receivedValues(x, y);
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
