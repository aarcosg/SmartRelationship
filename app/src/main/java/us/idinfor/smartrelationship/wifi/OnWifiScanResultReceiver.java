package us.idinfor.smartrelationship.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;
import us.idinfor.smartrelationship.WakefulIntentService;

public class OnWifiScanResultReceiver extends BroadcastReceiver {

    private static final String TAG = OnWifiScanResultReceiver.class.getCanonicalName();
    private SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnWifiScanResultReceiver@onReceive");
        WakefulIntentService.acquireStaticLock(context, Constants.LOCK_WIFI_SCAN_SERVICE);
        prefs = Utils.getSharedPreferences(context);
        if(prefs.getBoolean(Constants.PROPERTY_LISTENING, false)){
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                Log.i(TAG,"Wifi scan results available");
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                List<ScanResult> networks = wifiManager.getScanResults();

                Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
                Long timestamp = prefs.getLong(Constants.PROPERTY_TIMESTAMP,0L);

                if(networks != null && !networks.isEmpty()){
                    Log.i(TAG,"Write to " + Constants.WIFI_LOG_FOLDER + " log file. Wifi networks found. Listening ID = " + listeningId);
                    int wifiId = 1;
                    for(ScanResult scanResult : networks){
                        Utils.writeToLogFile(Constants.WIFI_LOG_FOLDER
                                ,timestamp + Constants.CSV_SEPARATOR
                                + listeningId + Constants.CSV_SEPARATOR
                                + wifiId + Constants.CSV_SEPARATOR
                                + scanResult.BSSID + Constants.CSV_SEPARATOR
                                + scanResult.SSID + Constants.CSV_SEPARATOR
                                + scanResult.level + Constants.CSV_SEPARATOR
                                + scanResult.frequency);
                        wifiId++;
                    }
                }else{
                    Log.i(TAG,"Write to " + Constants.WIFI_LOG_FOLDER + " log file. Wifi networks not found. Listening ID = " + listeningId);
                    Utils.writeToLogFile(Constants.WIFI_LOG_FOLDER
                            ,timestamp + Constants.CSV_SEPARATOR
                            + listeningId + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR);
                }
            }
        }


    }
}