package us.idinfor.smartrelationship.deviceorientation;


import android.content.Intent;
import android.hardware.SensorEvent;
import android.util.Log;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;
import us.idinfor.smartrelationship.WakefulIntentService;

public class OrientationService extends WakefulIntentService {

    private static final String TAG = OrientationService.class.getCanonicalName();

    public OrientationService() {
        super("OrientationService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Log.i(TAG, "OrientationService@doWakefulWork");
        SensorOrientation sensorOrientation = new SensorOrientation(this) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                super.onSensorChanged(event);
                if (this.isDataFetched()) {
                    this.stopListener();
                    Log.i(TAG,"Sensor data fetched, save on prefs");
                    Utils.getSharedPreferences(OrientationService.this).edit()
                            .putFloat(Constants.PROPERTY_ORIENTATION_AZIMUTH,this.getAzimuth())
                            .putFloat(Constants.PROPERTY_ORIENTATION_PITCH,this.getPitch())
                            .putFloat(Constants.PROPERTY_ORIENTATION_ROLL,this.getRoll())
                            .commit();
                }
            }
        };
        sensorOrientation.startListener();
    }
}
