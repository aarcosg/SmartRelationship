package us.idinfor.smartrelationship;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
        WakefulIntentService.acquireStaticLock(context);
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
            Log.i(TAG,"Wifi networks found");
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> networks = wifiManager.getScanResults();
            List<WifiNetwork> wifiNetworks = new ArrayList<>();
            for(ScanResult scanResult : networks){
                WifiNetwork wifiNetwork = new WifiNetwork(scanResult.BSSID,scanResult.SSID,scanResult.frequency,scanResult.level,scanResult.timestamp);
                wifiNetworks.add(wifiNetwork);
            }
            Gson gson = new Gson();
            LogRecord logRecord = new LogRecord(
                    Utils.getSharedPreferences(context).getLong(Constants.PROPERTY_LISTENING_ID,0L)
                    ,LogRecord.Type.WIFI
                    ,System.currentTimeMillis()
                    ,null
                    ,wifiNetworks);
            Utils.writeToLogFile(gson.toJson(logRecord));
            //TODO
            ComponentName component = new ComponentName(context, WifiScanService.class);
            int status = context.getPackageManager().getComponentEnabledSetting(component);
            if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                //Disable
                context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else if(status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                //Enable
                //context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);
            }

        }

    }
}