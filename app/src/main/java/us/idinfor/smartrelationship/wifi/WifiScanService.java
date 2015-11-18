package us.idinfor.smartrelationship.wifi;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;

public class WifiScanService extends IntentService {

    private static final String TAG = WifiScanService.class.getCanonicalName();

    public WifiScanService() {
        super("WifiScanService");
    }

    public static void startActionSampleWifi(Context context) {
        Intent intent = new Intent(context, WifiScanService.class);
        intent.setAction(Constants.ACTION_SAMPLE_WIFI);
        context.startService(intent);
    }

    public static void startActionWifiResults(Context context) {
        Intent intent = new Intent(context, WifiScanService.class);
        intent.setAction(Constants.ACTION_WIFI_RESULTS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_SAMPLE_WIFI.equals(action)) {
                handleActionSampleWifi();
            } else if (Constants.ACTION_WIFI_RESULTS.equals(action)) {
                handleActionWifiResults();
            }
        }
    }

    private void handleActionSampleWifi() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.startScan();
        } else {
            Log.i(TAG, "Wifi is disabled. Enable it programmatically");
            wifiManager.setWifiEnabled(true);
        }
    }

    private void handleActionWifiResults() {
        SharedPreferences prefs = Utils.getSharedPreferences(this);
        Log.i(TAG, "Wifi scan results available");
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> networks = wifiManager.getScanResults();

        Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
        Long timestamp = prefs.getLong(Constants.PROPERTY_TIMESTAMP, 0L);

        if (networks != null && !networks.isEmpty()) {
            Log.i(TAG, "Write to " + Constants.WIFI_LOG_FOLDER + " log file. Wifi networks found. Listening ID = " + listeningId);
            int wifiId = 1;
            for (ScanResult scanResult : networks) {
                Utils.writeToLogFile(Constants.WIFI_LOG_FOLDER
                        , timestamp + Constants.CSV_SEPARATOR
                        + listeningId + Constants.CSV_SEPARATOR
                        + wifiId + Constants.CSV_SEPARATOR
                        + scanResult.BSSID + Constants.CSV_SEPARATOR
                        + scanResult.SSID + Constants.CSV_SEPARATOR
                        + scanResult.level + Constants.CSV_SEPARATOR
                        + scanResult.frequency);
                wifiId++;
            }
        } else {
            Log.i(TAG, "Write to " + Constants.WIFI_LOG_FOLDER + " log file. Wifi networks not found. Listening ID = " + listeningId);
            Utils.writeToLogFile(Constants.WIFI_LOG_FOLDER
                    , timestamp + Constants.CSV_SEPARATOR
                    + listeningId + Constants.CSV_SEPARATOR
                    + Constants.CSV_SEPARATOR
                    + Constants.CSV_SEPARATOR
                    + Constants.CSV_SEPARATOR
                    + Constants.CSV_SEPARATOR);
        }
    }
}