package us.idinfor.smartrelationship;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int REQUEST_ENABLE_BT = 1;

    @InjectView(R.id.bluetooth_scan_frecuency_edit)
    EditText mBluetoothScanFrecuencyEdit;
    @InjectView(R.id.bluetooth_scan_frecuency_til)
    TextInputLayout mBluetoothScanFrecuencyTil;
    @InjectView(R.id.voice_record_duration_edit)
    EditText mVoiceRecordDurationEdit;
    @InjectView(R.id.voice_record_duration_til)
    TextInputLayout mVoiceRecordDurationTil;
    @InjectView(R.id.start_listening_btn)
    Button mStartListeningBtn;
    @InjectView(R.id.stop_listening_btn)
    Button mStopListeningBtn;
    @InjectView(R.id.save_btn)
    Button mSaveBtn;

    SharedPreferences prefs;
    boolean bluetoothEnabled = false;
    BluetoothManager mBluetoothManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = Utils.getSharedPreferences(this);
        ButterKnife.inject(this);
        buildActionBarToolbar(getString(R.string.app_name), false);
        setButtonsEnabledState();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

    }

    @OnClick(R.id.start_listening_btn)
    public void startListening() {
        checkBluetooth();
        if(bluetoothEnabled){
            Intent intent = new Intent();
            intent.setAction(Constants.START_LISTENING);
            sendBroadcast(intent);
            setListeningState(true);
            setButtonsEnabledState();
        }
    }

    @OnClick(R.id.stop_listening_btn)
    public void stopListening() {
        Intent intent = new Intent();
        intent.setAction(Constants.STOP_LISTENING);
        sendBroadcast(intent);
        setListeningState(false);
        setButtonsEnabledState();
        if(mBluetoothManager.getAdapter() != null){
            mBluetoothManager.getAdapter().cancelDiscovery();
        }
    }

    @OnClick(R.id.save_btn)
    public void savePreferences(final View view) {
        // Reset errors
        mBluetoothScanFrecuencyTil.setError(null);
        mVoiceRecordDurationTil.setError(null);

        String scanFrecuency = mBluetoothScanFrecuencyEdit.getText().toString();
        String recordDuration = mVoiceRecordDurationEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check preferences
        if (TextUtils.isEmpty(scanFrecuency) || (!TextUtils.isEmpty(scanFrecuency) && Integer.valueOf(scanFrecuency) <= 0)) {
            mBluetoothScanFrecuencyTil.setError(getString(R.string.error_scan_frecuency));
            cancel = true;
            focusView = mBluetoothScanFrecuencyEdit;
        } else if (TextUtils.isEmpty(recordDuration) || (!TextUtils.isEmpty(recordDuration) && Integer.valueOf(recordDuration) <= 0)) {
            mVoiceRecordDurationTil.setError(getString(R.string.error_record_duration));
            cancel = true;
            focusView = mVoiceRecordDurationEdit;
        } else if (Integer.valueOf(scanFrecuency) <= Integer.valueOf(recordDuration)){
            mBluetoothScanFrecuencyTil.setError(getString(R.string.error_scan_freq_lower_record_duration));
            cancel = true;
            focusView = mBluetoothScanFrecuencyEdit;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Utils.hideSoftKeyboard(this);
            prefs.edit()
                    .putInt(Constants.PROPERTY_BLUETOOTH_SCAN_FRECUENCY, Integer.valueOf(scanFrecuency))
                    .putInt(Constants.PROPERTY_VOICE_RECORD_DURATION, Integer.valueOf(recordDuration))
                    .apply();
            Snackbar
                    .make(view, getString(R.string.preferences_saved), Snackbar.LENGTH_LONG).show();
        }

    }

    /**
     * Ensures that only one button is enabled at any time.
     */
    private void setButtonsEnabledState() {
        if (getListeningState()) {
            mStartListeningBtn.setEnabled(false);
            mStopListeningBtn.setEnabled(true);
            mBluetoothScanFrecuencyEdit.setEnabled(false);
            mVoiceRecordDurationEdit.setEnabled(false);
            mSaveBtn.setEnabled(false);
        } else {
            mStartListeningBtn.setEnabled(true);
            mStopListeningBtn.setEnabled(false);
            mBluetoothScanFrecuencyEdit.setEnabled(true);
            mVoiceRecordDurationEdit.setEnabled(true);
            mSaveBtn.setEnabled(true);

        }
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are scanning
     * bluetooth devices and wifi networks
     */
    private boolean getListeningState() {
        return Utils.getSharedPreferences(this)
                .getBoolean(Constants.PROPERTY_LISTENING, false);
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether we are scanning
     * bluetooth devices and wifi networks
     */
    private void setListeningState(boolean listening) {
        Utils.getSharedPreferences(this)
                .edit()
                .putBoolean(Constants.PROPERTY_LISTENING, listening)
                .apply();
    }

    private void checkBluetooth(){
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothEnabled = false;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            bluetoothEnabled = false;
        } else if (!mBluetoothAdapter.isEnabled()){
            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            bluetoothEnabled = false;
        } else {
            bluetoothEnabled = true;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            bluetoothEnabled = false;
            return;
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            bluetoothEnabled = true;
            startListening();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
