package us.idinfor.smartrelationship.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;
import us.idinfor.smartrelationship.WakefulIntentService;

public class OnBluetoothScanResultReceiver extends BroadcastReceiver {

    private static final String TAG = OnBluetoothScanResultReceiver.class.getCanonicalName();

    private static Set<BTDevice> devices;
    private SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnBluetoothScanResultReceiver@onReceive");
        WakefulIntentService.acquireStaticLock(context, Constants.LOCK_BLUETOOTH_SCAN_SERVICE);
        prefs = Utils.getSharedPreferences(context);
        if(prefs.getBoolean(Constants.PROPERTY_LISTENING, false)){
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
                Log.i(TAG, "Bluetooth discovery started");
                devices = new HashSet<BTDevice>();

            } else if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "Bluetooth device found: " + device.getName() + " - " + device.getAddress());
                devices.add(new BTDevice(
                        device.getName()
                        , device.getAddress()
                        , Utils.getMajorBluetoothClassString(context, device.getBluetoothClass().getMajorDeviceClass())));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                Log.i(TAG, "Bluetooth discovery finished");

                Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
                Long timestamp = prefs.getLong(Constants.PROPERTY_TIMESTAMP,0L);

                if(devices != null && !devices.isEmpty()){
                    Log.i(TAG,"Write to " + Constants.BLUETOOTH_LOG_FOLDER + " log file. Bluetooth devices found. Listening ID = " + listeningId);
                    for(BTDevice bt : devices){
                        Utils.writeToLogFile(Constants.BLUETOOTH_LOG_FOLDER
                                , timestamp + Constants.CSV_SEPARATOR
                                + listeningId + Constants.CSV_SEPARATOR
                                + bt.getName() + Constants.CSV_SEPARATOR
                                + bt.getAddress() + Constants.CSV_SEPARATOR
                                + bt.getMajorClass());
                    }
                } else if(devices != null) {
                    Log.i(TAG,"Write to " + Constants.BLUETOOTH_LOG_FOLDER + " log file. Bluetooth devices not found. Listening ID = " + listeningId);
                    Utils.writeToLogFile(Constants.BLUETOOTH_LOG_FOLDER
                            , timestamp + Constants.CSV_SEPARATOR
                            + listeningId + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR
                            + Constants.CSV_SEPARATOR);
                }
                devices = null;
            }else{
                Log.i(TAG,"Write to " + Constants.BLUETOOTH_LOG_FOLDER + " log file. Action not filtered = " + intent.getAction());
                Utils.writeToLogFile(Constants.BLUETOOTH_LOG_FOLDER
                        , -1L + Constants.CSV_SEPARATOR
                        + -1L + Constants.CSV_SEPARATOR
                        + "ERROR" + Constants.CSV_SEPARATOR
                        + intent.getAction() + Constants.CSV_SEPARATOR);
            }
        }
    }
}