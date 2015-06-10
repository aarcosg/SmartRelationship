package us.idinfor.smartrelationship;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class OnWifiScanResultReceiver extends BroadcastReceiver {

    private static final String TAG = OnWifiScanResultReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnWifiScanResultReceiver@onReceive");
        WakefulIntentService.acquireStaticLock(context,Constants.LOCK_WIFI_SCAN_SERVICE);
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
            Log.i(TAG,"Wifi networks found");
            SharedPreferences prefs = Utils.getSharedPreferences(context);
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> networks = wifiManager.getScanResults();
            List<WifiNetwork> wifiNetworks = new ArrayList<>();
            for(ScanResult scanResult : networks){
                WifiNetwork wifiNetwork = new WifiNetwork(scanResult.BSSID,scanResult.SSID,scanResult.frequency,scanResult.level,scanResult.timestamp);
                wifiNetworks.add(wifiNetwork);
            }
            Gson gson = new Gson();
            Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
            LogRecord logRecord = new LogRecord(
                    listeningId
                    ,LogRecord.Type.WIFI
                    ,System.currentTimeMillis()
                    ,null
                    ,wifiNetworks);
            Utils.writeToLogFile(Constants.WIFI_LOG_FOLDER,listeningId,gson.toJson(logRecord));
        }

    }
}