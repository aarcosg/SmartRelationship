package us.idinfor.smartrelationship.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import us.idinfor.smartrelationship.WakefulIntentService;

public class WifiScanService extends WakefulIntentService {

    private static final String TAG = WifiScanService.class.getCanonicalName();

    public WifiScanService() {
        super("WifiScanService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Log.i(TAG,"WifiScanService@doWakefulWork");
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()){
            wifiManager.startScan();
        }else{
            Log.e(TAG,"Wifi is disabled");
        }


    }




}