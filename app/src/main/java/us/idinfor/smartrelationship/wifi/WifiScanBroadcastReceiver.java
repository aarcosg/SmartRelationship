package us.idinfor.smartrelationship.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;

public class WifiScanBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = WifiScanBroadcastReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnWifiScanResultReceiver@onReceive");
        //WakefulIntentService.acquireStaticLock(context, Constants.LOCK_WIFI_SCAN_SERVICE);
        if (Utils.getSharedPreferences(context).getBoolean(Constants.PROPERTY_LISTENING, false) && WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
            WifiScanService.startActionWifiResults(context.getApplicationContext());
        }

    }
}