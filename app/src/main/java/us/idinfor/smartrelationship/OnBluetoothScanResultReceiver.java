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

    private static Set<BluetoothDevice> devices;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnBluetoothScanResultReceiver@onReceive");
        WakefulIntentService.acquireStaticLock(context);

        if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.i(TAG, "Bluetooth device found: " + device.getName() + " - " + device.getAddress());
            getDevices().add(device);
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
            Log.i(TAG, "Bluetooth discovery started");

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
            Log.i(TAG, "Bluetooth discovery finished");
            Gson gson = new Gson();
            LogRecord logRecord = new LogRecord(
                     Utils.getSharedPreferences(context).getLong(Constants.PROPERTY_LISTENING_ID,0L)
                    ,LogRecord.Type.BLUETHOOTH
                    ,System.currentTimeMillis()
                    ,devices
                    ,null);
            Utils.writeToLogFile(gson.toJson(logRecord));
            /*StringBuilder stringBuilder = new StringBuilder();
            //Date
            stringBuilder.append(System.currentTimeMillis());
            stringBuilder.append(";");
            //Listening ID
            stringBuilder.append(Utils.getSharedPreferences(context).getLong(Constants.PROPERTY_LISTENING_ID,0L));
            stringBuilder.append(";");
            //Num Devices found
            stringBuilder.append(devices.size());
            stringBuilder.append(";");
            //Devices found
            for(BluetoothDevice bd : devices){
                stringBuilder.append("[");
                stringBuilder.append(bd.getAddress());
                stringBuilder.append("@");
                stringBuilder.append(bd.getName());
                stringBuilder.append("]");
            }
            stringBuilder.append(";");
            stringBuilder.append("\n");
            Utils.writeToLogFile(stringBuilder.toString());*/
        }

    }

    private Set<BluetoothDevice> getDevices(){
        if(devices == null){
            devices = new HashSet<BluetoothDevice>();
        }
        return devices;
    }
}