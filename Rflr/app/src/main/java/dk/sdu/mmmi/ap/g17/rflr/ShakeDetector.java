package dk.sdu.mmmi.ap.g17.rflr;
/**
 * Created by Morten on 01-05-2017.
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Detects shake events happening.
 * An OnShakeListener must be set using setOnShakeListener
 */
public class ShakeDetector implements SensorEventListener {

    private static final float SHAKE_THRESHOLD_GRAVITY = 1.5F;  //Adjust to change sensitivity.
    private static final int SHAKE_SLEEP_TIME_MS = 500; //Only one shake pr SHAKE_SLEEP_TIME_MS is detected

    private OnShakeListener mShakeListener;
    private long mShakeTimeStamp;

    public ShakeDetector() {
        this.mShakeTimeStamp = System.currentTimeMillis();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.mShakeListener = listener;
    }

    /**
     * Checks if the sensor has changed enough to actually register as a shake
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mShakeListener != null) {
            //Get values
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double gX = x / SensorManager.GRAVITY_EARTH;
            double gY = y / SensorManager.GRAVITY_EARTH;
            double gZ = z / SensorManager.GRAVITY_EARTH;
            //gForce will be close to 1 when no movement
            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);
            //Shake is enough to register
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long time = System.currentTimeMillis();
                //Shake is within the time limit after last shake
                if (mShakeTimeStamp + SHAKE_SLEEP_TIME_MS > time) {
                    return;
                }
                mShakeTimeStamp = time; //REset timestamp
                mShakeListener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Ignore for now
    }


}


