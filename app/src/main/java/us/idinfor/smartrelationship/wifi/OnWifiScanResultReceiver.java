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
                Log.i(TAG,"Wifi networks found");
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                List<ScanResult> networks = wifiManager.getScanResults();
                /*final List<WifiNetwork> wifiNetworks = new ArrayList<>();
                for(ScanResult scanResult : networks){
                    WifiNetwork wifiNetwork = new WifiNetwork(scanResult.BSSID
                            ,scanResult.SSID
                            ,scanResult.frequency
                            ,scanResult.level
                            ,scanResult.timestamp);
                    wifiNetworks.add(wifiNetwork);
                }
                Gson gson = new Gson();*/
                Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
                Long timestamp = prefs.getLong(Constants.PROPERTY_TIMESTAMP,0L);
                /*LogRecord logRecord = new LogRecord(
                        listeningId
                        ,LogRecord.Type.WIFI
                        ,System.currentTimeMillis()
                        ,null
                        ,wifiNetworks
                        ,null
                        ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_AZIMUTH, 0.0f)
                        ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_PITCH, 0.0f)
                        ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_ROLL, 0.0f));
                Utils.writeToLogFile(Constants.WIFI_LOG_FOLDER
                        , Utils.getTimeStamp() + ";" + listeningId + ";" + gson.toJson(logRecord));*/
                if(networks != null && !networks.isEmpty()){
                    for(ScanResult scanResult : networks){
                        Utils.writeToLogFile(Constants.WIFI_LOG_FOLDER
                                ,timestamp + Constants.CSV_SEPARATOR
                                + listeningId + Constants.CSV_SEPARATOR
                                + scanResult.BSSID + Constants.CSV_SEPARATOR
                                + scanResult.SSID + Constants.CSV_SEPARATOR
                                + scanResult.level + Constants.CSV_SEPARATOR
                                + scanResult.frequency);
                    }
                }else{
                    Utils.writeToLogFile(Constants.WIFI_LOG_FOLDER
                            ,timestamp + Constants.CSV_SEPARATOR
                            + listeningId + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR);
                }
            }
        }


    }
}