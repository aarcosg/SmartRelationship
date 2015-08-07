package us.idinfor.smartrelationship.deviceorientation;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.util.Log;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;

public class OrientationService extends IntentService {

    private static final String TAG = OrientationService.class.getCanonicalName();

    public OrientationService() {
        super("OrientationService");
    }

    public static void startActionSampleOrientation(Context context) {
        Intent intent = new Intent(context, OrientationService.class);
        intent.setAction(Constants.ACTION_SAMPLE_ORIENTATION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_SAMPLE_ORIENTATION.equals(action)) {
                handleActionSampleOrientation();
            }
        }
    }

    private void handleActionSampleOrientation() {
        SensorOrientation sensorOrientation = new SensorOrientation(this) {
            @Override
            public void onSensorChanged(SensorEvent event) {
                super.onSensorChanged(event);
                if (this.isDataFetched()) {
                    this.stopListener();
                    Log.i(TAG,"Sensor data fetched, save on prefs");
                    SharedPreferences prefs = Utils.getSharedPreferences(OrientationService.this);
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
