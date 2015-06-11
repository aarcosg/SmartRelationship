package us.idinfor.smartrelationship.deviceorientation;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorOrientation implements SensorEventListener{

    private static final String TAG = SensorOrientation.class.getCanonicalName();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private boolean dataFetched;
    private float azimuth;
    private float pitch;
    private float roll;

    public SensorOrientation(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        dataFetched = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // orientation contains: azimuth, pitch and roll
                azimuth = orientation[0];
                pitch = orientation[1];
                roll = orientation[2];
                Log.i(TAG, "Azimuth = " + azimuth + " Pitch = " + pitch + " Roll = " + roll);
                dataFetched = true;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void startListener(){
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,mMagnetometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopListener(){
        mSensorManager.unregisterListener(this);
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public boolean isDataFetched(){
        return dataFetched;
    }
}
