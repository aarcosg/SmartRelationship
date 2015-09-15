package us.idinfor.smartrelationship;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import us.idinfor.smartrelationship.activityrecognition.ActivityRecognitionService;
import us.idinfor.smartrelationship.audio.AudioMediaRecorderService;
import us.idinfor.smartrelationship.bluetooth.BluetoothScanService;
import us.idinfor.smartrelationship.deviceorientation.OrientationService;
import us.idinfor.smartrelationship.wifi.WifiScanService;

public class OnAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = OnAlarmReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnAlarmReceiver@onReceive");
        SharedPreferences prefs = Utils.getSharedPreferences(context);
        prefs.edit().putLong(Constants.PROPERTY_LISTENING_ID,prefs.getLong(Constants.PROPERTY_LISTENING_ID,0L)+1L)
                .putLong(Constants.PROPERTY_TIMESTAMP,System.currentTimeMillis())
                .commit();
        Log.i(TAG,"new Listening ID = " + prefs.getLong(Constants.PROPERTY_LISTENING_ID,0L));
        if(prefs.getBoolean(Constants.PROPERTY_RECORD_AUDIO_ENABLED, false)){
            //AudioRecorderService.startActionRecordWav(context.getApplicationContext());
            AudioMediaRecorderService.startActionMediaRecordMp3(context.getApplicationContext());
        }
        OrientationService.startActionSampleOrientation(context.getApplicationContext());
        WifiScanService.startActionSampleWifi(context.getApplicationContext());
        BluetoothScanService.startActionSampleBluetooth(context.getApplicationContext());
        ActivityRecognitionService.startActionSampleActivitySave(context.getApplicationContext());

    }

}