package us.idinfor.smartrelationship.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

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
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "Bluetooth device found: " + device.getName() + " - " + device.getAddress());
                getDevices().add(new BTDevice(
                        device.getName()
                        ,device.getAddress()
                        ,Utils.getMajorBluetoothClassString(context,device.getBluetoothClass().getMajorDeviceClass())));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
                Log.i(TAG, "Bluetooth discovery started");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                Log.i(TAG, "Bluetooth discovery finished");
                Gson gson = new Gson();
                Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
                Long timestamp = prefs.getLong(Constants.PROPERTY_TIMESTAMP,0L);
                /*LogRecord logRecord = new LogRecord(
                        listeningId
                        ,LogRecord.Type.BLUETHOOTH
                        ,System.currentTimeMillis()
                        ,devices
                        ,null
                        ,null
                        ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_AZIMUTH,0.0f)
                        ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_PITCH, 0.0f)
                        ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_ROLL, 0.0f));*/
                if(devices != null && !devices.isEmpty()){
                    int btId = 1;
                    for(BTDevice bt : devices){
                        Utils.writeToLogFile(Constants.BLUETOOTH_LOG_FOLDER
                                ,timestamp + Constants.CSV_SEPARATOR
                                + listeningId + Constants.CSV_SEPARATOR
                                + btId + Constants.CSV_SEPARATOR
                                + bt.getName() + Constants.CSV_SEPARATOR
                                + bt.getAddress() + Constants.CSV_SEPARATOR
                                + bt.getMajorClass());
                        btId++;
                    }
                    devices.clear();
                }
            }
        }
    }

    private Set<BTDevice> getDevices(){
        if(devices == null){
            devices = new HashSet<BTDevice>();
        }
        return devices;
    }
}