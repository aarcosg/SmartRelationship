package us.idinfor.smartrelationship;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

public class OnBluetoothScanResultReceiver extends BroadcastReceiver {

    private static final String TAG = OnBluetoothScanResultReceiver.class.getCanonicalName();

    private static Set<BTDevice> devices;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnBluetoothScanResultReceiver@onReceive");
        WakefulIntentService.acquireStaticLock(context,Constants.LOCK_BLUETOOTH_SCAN_SERVICE);
        if(Utils.getSharedPreferences(context).getBoolean(Constants.PROPERTY_LISTENING, false)){
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "Bluetooth device found: " + device.getName() + " - " + device.getAddress());
                getDevices().add(new BTDevice(device.getName(),device.getAddress()));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
                Log.i(TAG, "Bluetooth discovery started");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                Log.i(TAG, "Bluetooth discovery finished");
                Gson gson = new Gson();
                Long listeningId = Utils.getSharedPreferences(context).getLong(Constants.PROPERTY_LISTENING_ID,0L);
                LogRecord logRecord = new LogRecord(
                        listeningId
                        ,LogRecord.Type.BLUETHOOTH
                        ,System.currentTimeMillis()
                        ,devices
                        ,null
                        ,null);
                Utils.writeToLogFile(Constants.BLUETOOTH_LOG_FOLDER
                        ,Utils.getTimeStamp() + ";" + listeningId + ";" + gson.toJson(logRecord));
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