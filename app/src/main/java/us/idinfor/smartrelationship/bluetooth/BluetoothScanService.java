package us.idinfor.smartrelationship.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;

public class BluetoothScanService extends Service {

    private static final String TAG = BluetoothScanService.class.getCanonicalName();

    private BluetoothAdapter mBluetoothAdapter;
    private int startId;

    public static void startActionSampleBluetooth(Context context) {
        Intent intent = new Intent(context, BluetoothScanService.class);
        intent.setAction(Constants.ACTION_SAMPLE_BLUETOOTH);
        context.startService(intent);
    }

    public static void startActionBluetoothFound(Context context, BluetoothDevice device, Short RSSI){
        Intent intent = new Intent(context, BluetoothScanService.class);
        intent.setAction(Constants.ACTION_SAMPLE_BLUETOOTH_FOUND);
        intent.putExtra(Constants.EXTRA_BLUETOOTH_DEVICE, device);
        intent.putExtra(Constants.EXTRA_BLUETOOTH_RSSI, RSSI);
        context.startService(intent);
    }

    public static void startActionSampleBluetoothFinished(Context context){
        Intent intent = new Intent(context, BluetoothScanService.class);
        intent.setAction(Constants.ACTION_SAMPLE_BLUETOOTH_FINISHED);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_SAMPLE_BLUETOOTH.equals(action)) {
                this.startId = startId;
                handleActionSampleBluetooth();
            }else if(Constants.ACTION_SAMPLE_BLUETOOTH_FOUND.equals(action)){
                handleActionBluetoothDeviceFound((BluetoothDevice)intent.getParcelableExtra(Constants.EXTRA_BLUETOOTH_DEVICE),
                        intent.getShortExtra(Constants.EXTRA_BLUETOOTH_RSSI,Short.MIN_VALUE));
            }else if(Constants.ACTION_SAMPLE_BLUETOOTH_FINISHED.equals(action)){
                handleActionBluetoothDiscoveryFinished();
            }
        }
        return START_STICKY;
    }


    private void handleActionSampleBluetooth(){
        Log.i(TAG, "BluetoothScanService@doWakefulWork");
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            stopSelf();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device and is enabled
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            stopSelf();
        }

        /*Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.i(TAG, pairedDevices.size() + " devices bonded");*/

        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();

    }

    /*private final BroadcastReceiver mReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,intent.getAction());
            SharedPreferences prefs = Utils.getSharedPreferences(context);
            if(prefs.getBoolean(Constants.PROPERTY_LISTENING, false)){
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
                    Log.i(TAG, "Bluetooth discovery started");
                } else if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                    Log.i(TAG, "New bluetooth device found");
                    BluetoothScanService.this.onBluetoothDeviceFound((BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                    Log.i(TAG, "Bluetooth discovery finished");
                    BluetoothScanService.this.onBluetoothDiscoveryFinished();
                } else {
                    Log.e(TAG,"Intent action uncatched: " + intent.getAction());
                }
            }
        }
    };

    private void registerBroadcastReceiver(){
        registerReceiver(mReciever, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mReciever, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReciever, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }*/

    private void handleActionBluetoothDeviceFound(BluetoothDevice device, Short RSSI){
        Log.i(TAG, "New bluetooth device added: " + device.getName() + " - " + device.getAddress());
        Set<BTDevice> devices = null;
        Gson gson = new Gson();
        SharedPreferences prefs = Utils.getSharedPreferences(this);
        if(prefs.contains(Constants.PROPERTY_BLUETOOTH_LIST)){
            Type setType = new TypeToken<HashSet<BTDevice>>() {}.getType();
            devices = gson.fromJson(prefs.getString(Constants.PROPERTY_BLUETOOTH_LIST, ""), setType);
        }else{
            devices = new HashSet<BTDevice>();
        }
        BTDevice btDevice = new BTDevice(device.getName()
                ,device.getAddress()
                ,Utils.getMajorBluetoothClassString(this, device.getBluetoothClass().getMajorDeviceClass())
                ,RSSI);
        devices.add(btDevice);
        prefs.edit().putString(Constants.PROPERTY_BLUETOOTH_LIST, gson.toJson(devices)).apply();
    }

    private void handleActionBluetoothDiscoveryFinished(){
        SharedPreferences prefs = Utils.getSharedPreferences(this);
        Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
        Long timestamp = prefs.getLong(Constants.PROPERTY_TIMESTAMP, 0L);

        Set<BTDevice> devices = null;
        if(prefs.contains(Constants.PROPERTY_BLUETOOTH_LIST)){
            Type setType = new TypeToken<HashSet<BTDevice>>() {}.getType();
            devices = new Gson().fromJson(prefs.getString(Constants.PROPERTY_BLUETOOTH_LIST, ""), setType);
        }

        if(devices != null && !devices.isEmpty()){
            Log.i(TAG, "Write to " + Constants.BLUETOOTH_LOG_FOLDER + " log file. Bluetooth devices found. Listening ID = " + listeningId);
            for (BTDevice bt : devices) {
                Utils.writeToLogFile(Constants.BLUETOOTH_LOG_FOLDER
                        , timestamp + Constants.CSV_SEPARATOR
                        + listeningId + Constants.CSV_SEPARATOR
                        + bt.getName() + Constants.CSV_SEPARATOR
                        + bt.getAddress() + Constants.CSV_SEPARATOR
                        + bt.getMajorClass() + Constants.CSV_SEPARATOR
                        + bt.getRssi());
            }
        } else {
            Log.i(TAG,"Write to " + Constants.BLUETOOTH_LOG_FOLDER + " log file. Bluetooth devices not found. Listening ID = " + listeningId);
            Utils.writeToLogFile(Constants.BLUETOOTH_LOG_FOLDER
                    , timestamp + Constants.CSV_SEPARATOR
                    + listeningId + Constants.CSV_SEPARATOR
                    + Constants.CSV_SEPARATOR
                    + Constants.CSV_SEPARATOR);
        }
        prefs.edit().remove(Constants.PROPERTY_BLUETOOTH_LIST).commit();
        stopSelf(startId);
    }

}