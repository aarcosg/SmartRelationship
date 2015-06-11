package us.idinfor.smartrelationship.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import us.idinfor.smartrelationship.WakefulIntentService;

public class BluetoothScanService extends WakefulIntentService {

    private static final String TAG = BluetoothScanService.class.getCanonicalName();

    private BluetoothAdapter mBluetoothAdapter;

    public BluetoothScanService() {
        super("BluetoothScanService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Log.i(TAG,"BluetoothScanService@doWakefulWork");
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




}