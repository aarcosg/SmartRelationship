package us.idinfor.smartrelationship;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import us.idinfor.smartrelationship.activityrecognition.ActivityRecognitionService;
import us.idinfor.smartrelationship.audio.AudioRecorderService;
import us.idinfor.smartrelationship.bluetooth.BluetoothScanService;
import us.idinfor.smartrelationship.deviceorientation.OrientationService;
import us.idinfor.smartrelationship.wifi.WifiScanService;

public class OnListeningStateChangedReceiver extends BroadcastReceiver {

    private static final String TAG = OnListeningStateChangedReceiver.class.getCanonicalName();
    private AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"OnListeningStateChangedReceiver@onReceive : " + intent.getAction());
        if(!Utils.isWeekend()){
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, OnAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            if (TextUtils.equals(intent.getAction(), Constants.ACTION_START_LISTENING)) {
                Log.i(TAG, "Set Alarm Manager");
                setListeningState(context, true);
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 5 * 1000,
                        Utils.getSharedPreferences(context).getInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, Constants.DEFAULT_SAMPLE_SCAN_FREQUENCY) * 1000,
                        pi);
                ActivityRecognitionService.startActionSampleActivity(context.getApplicationContext());
            } else if (TextUtils.equals(intent.getAction(), Constants.ACTION_STOP_LISTENING)) {
                Log.i(TAG, "Cancel Alarm Manager");
                setListeningState(context, false);
                alarmManager.cancel(pi);
                Log.i(TAG, "Cancel Activity Recognition");
                ActivityRecognitionService.startActionSampleActivityStop(context.getApplicationContext());
                Log.i(TAG, "Cancel Bluetooth Discovery");
                BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                if(mBluetoothManager.getAdapter() != null){
                    mBluetoothManager.getAdapter().cancelDiscovery();
                }
            }
            setBackgroundServicesState(context, Utils.getSharedPreferences(context).getBoolean(Constants.PROPERTY_LISTENING, true));
        }else{
            Log.i(TAG,"It is weekend! I will not work until monday :)");
        }
    }

    private void setBackgroundServicesState(Context context, boolean listening){
        List<Class<?>> services = new ArrayList<Class<?>>();
        services.add(WifiScanService.class);
        services.add(BluetoothScanService.class);
        services.add(ActivityRecognitionService.class);
        services.add(AudioRecorderService.class);
        services.add(OrientationService.class);

        int flag=(listening ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

        PackageManager pm = context.getPackageManager();
        for(Class<?> service : services){
            ComponentName component = new ComponentName(context,service);
            pm.setComponentEnabledSetting(component, flag, PackageManager.DONT_KILL_APP);
        }

    }

    private void setListeningState(Context context, boolean listening) {
        Utils.getSharedPreferences(context)
                .edit()
                .putBoolean(Constants.PROPERTY_LISTENING, listening)
                .apply();
    }

}