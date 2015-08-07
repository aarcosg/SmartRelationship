package us.idinfor.smartrelationship.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;

public class BluetoothScanBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = BluetoothScanBroadcastReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive : " + intent.getAction());
        SharedPreferences prefs = Utils.getSharedPreferences(context);
        if(prefs.getBoolean(Constants.PROPERTY_LISTENING, false)){
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
                Log.i(TAG, "Bluetooth discovery started");
            } else if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                Log.i(TAG, "New bluetooth device found");
                BluetoothScanService.startActionBluetoothFound(context,(BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                Log.i(TAG, "Bluetooth discovery finished");
                BluetoothScanService.startActionSampleBluetoothFinished(context);
            } else {
                Log.e(TAG,"Intent action uncatched: " + intent.getAction());
            }
        }
    }
}