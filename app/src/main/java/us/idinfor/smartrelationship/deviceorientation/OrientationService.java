package us.idinfor.smartrelationship.deviceorientation;


import android.content.Intent;
import android.content.SharedPreferences;
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
                    SharedPreferences prefs = Utils.getSharedPreferences(OrientationService.this);
                    /*prefs.edit()
                            .putFloat(Constants.PROPERTY_ORIENTATION_AZIMUTH,this.getAzimuth())
                            .putFloat(Constants.PROPERTY_ORIENTATION_PITCH,this.getPitch())
                            .putFloat(Constants.PROPERTY_ORIENTATION_ROLL,this.getRoll())
                            .commit();*/
                    Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
                    Long timestamp = prefs.getLong(Constants.PROPERTY_TIMESTAMP,0L);
                    Utils.writeToLogFile(Constants.ORIENTATION_LOG_FOLDER
                            ,timestamp + Constants.CSV_SEPARATOR
                            + listeningId + Constants.CSV_SEPARATOR
                            + this.getPitch() + Constants.CSV_SEPARATOR
                            + this.getRoll() + Constants.CSV_SEPARATOR
                            + this.getAzimuth());
                }
            }
        };
        sensorOrientation.startListener();
    }
}
