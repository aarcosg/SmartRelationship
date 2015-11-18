package us.idinfor.smartrelationship;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import us.idinfor.smartrelationship.zip.ZipLogsAsyncTask;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DISCOVERABLE_BT = 2;
    private static final int REQUEST_SHARE_ZIP = 3;

    @InjectView(R.id.start_listening_btn)
    Button mStartListeningBtn;
    @InjectView(R.id.stop_listening_btn)
    Button mStopListeningBtn;
    @InjectView(R.id.enable_voice_recording)
    SwitchCompat mVoiceRecordingSwitch;
    @InjectView(R.id.zip_btn)
    Button mZipBtn;
    @InjectView(R.id.version_text)
    TextView mVersionText;


    SharedPreferences prefs;
    boolean bluetoothEnabled = false;
    BluetoothAdapter mBluetoothAdapter;
    File zipFile;
    @InjectView(R.id.activity_status)
    View mActivityStatus;
    @InjectView(R.id.activity_sample)
    TextView mActivitySample;
    @InjectView(R.id.audio_status)
    View mAudioStatus;
    @InjectView(R.id.audio_sample)
    TextView mAudioSample;
    @InjectView(R.id.bluetooth_status)
    View mBluetoothStatus;
    @InjectView(R.id.bluetooth_sample)
    TextView mBluetoothSample;
    @InjectView(R.id.orientation_status)
    View mOrientationStatus;
    @InjectView(R.id.orientation_sample)
    TextView mOrientationSample;
    @InjectView(R.id.wifi_status)
    View mWifiStatus;
    @InjectView(R.id.wifi_sample)
    TextView mWifiSample;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = Utils.getSharedPreferences(this);
        ButterKnife.inject(this);
        buildActionBarToolbar(getString(R.string.app_name), false);
        setButtonsEnabledState();
        loadPreferences();
        checkPlayServices();
        mVoiceRecordingSwitch.setChecked(prefs.getBoolean(Constants.PROPERTY_RECORD_AUDIO_ENABLED, false));
        try {
            mVersionText.setText("v. " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (getIntent().getIntExtra(Constants.EXTRA_NOTIFICATION, 0) > 0) {
            if (mBluetoothAdapter == null) {
                final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    checkBluetoothDiscoverable();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setButtonsEnabledState();
        checkLogStatus();
    }

    @OnCheckedChanged(R.id.enable_voice_recording)
    public void onVoiceRecordingChecked(boolean checked) {
        prefs.edit().putBoolean(Constants.PROPERTY_RECORD_AUDIO_ENABLED, checked).apply();
        Snackbar.make(mVoiceRecordingSwitch, getString(R.string.preferences_saved), Snackbar.LENGTH_LONG).show();
    }

    @OnClick(R.id.start_listening_btn)
    public void startListening() {
        checkBluetooth();
        if (bluetoothEnabled) {
            WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
            if(!wifiManager.isWifiEnabled()){
                wifiManager.setWifiEnabled(true);
            }
            setListeningState(true);
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_START_LISTENING);
            sendBroadcast(intent);
            setButtonsEnabledState();
        }
    }

    @OnClick(R.id.stop_listening_btn)
    public void stopListening() {
        setListeningState(false);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_STOP_LISTENING);
        sendBroadcast(intent);
        setButtonsEnabledState();
    }

    @OnClick(R.id.zip_btn)
    public void zipAndSendLogs() {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Zipping files...");
        new ZipLogsAsyncTask(progress) {
            @Override
            public void onPostExecute(String filepath) {
                super.onPostExecute(filepath);
                zipFile = new File(filepath);
                sendMail(zipFile);
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                checkLogStatus();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Ensures that only one button is enabled at any time.
     */
    private void setButtonsEnabledState() {
        if (getListeningState()) {
            mStartListeningBtn.setEnabled(false);
            mStopListeningBtn.setEnabled(true);
        } else {
            mStartListeningBtn.setEnabled(true);
            mStopListeningBtn.setEnabled(false);
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

    private void checkBluetooth() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoothEnabled = false;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            bluetoothEnabled = false;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            bluetoothEnabled = false;
        } else {
            checkBluetoothDiscoverable();
        }

    }

    private void checkBluetoothDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE_BT);
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
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            checkBluetoothDiscoverable();
            if(bluetoothEnabled){
                startListening();
            }
            return;
        } else if (requestCode == REQUEST_ENABLE_DISCOVERABLE_BT && resultCode != Activity.RESULT_CANCELED) {
            bluetoothEnabled = true;
            startListening();
            return;
        } else if (requestCode == REQUEST_SHARE_ZIP && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "Zip file sent");
            /*if (zipFile != null && zipFile.exists()) {
                if (zipFile.delete()) {
                    Snackbar.make(mVoiceRecordingSwitch, getString(R.string.logs_sent), Snackbar.LENGTH_LONG).show();
                }
            }*/
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadPreferences() {
        //Save default values of sample scan frequency and voice record duration
        if (prefs.getInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, Constants.DEFAULT_SAMPLE_SCAN_FREQUENCY) != Constants.DEFAULT_SAMPLE_SCAN_FREQUENCY) {
            prefs.edit().putInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, Constants.DEFAULT_SAMPLE_SCAN_FREQUENCY).apply();
        }
        if (prefs.getInt(Constants.PROPERTY_VOICE_RECORD_DURATION, Constants.DEFAULT_VOICE_RECORD_DURATION) != Constants.DEFAULT_VOICE_RECORD_DURATION) {
            prefs.edit().putInt(Constants.PROPERTY_VOICE_RECORD_DURATION, Constants.DEFAULT_VOICE_RECORD_DURATION).apply();
        }
    }

    /**
     * Verify that Google Play services is available.
     *
     * @return true if Google Play services is available, otherwise false
     */
    protected boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private void sendMail(File file) {
        Uri uriToZip = Uri.fromFile(file);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/zip");
        //emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SmartRelationship logs");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "SmartRelationship logs attached: " + file.getName());
        emailIntent.putExtra(Intent.EXTRA_STREAM, uriToZip);
        startActivityForResult(Intent.createChooser(emailIntent, "Send Logs:"), REQUEST_SHARE_ZIP);
    }

    private void checkLogStatus() {

        mProgressBar.setVisibility(View.VISIBLE);

        String activity = Utils.isLogWorking(Constants.ACTIVITY_LOG_FOLDER);
        String audio = Utils.isLogWorking(Constants.AUDIO_RECORDER_FOLDER);
        String bluetooth = Utils.isLogWorking(Constants.BLUETOOTH_LOG_FOLDER);
        String orientation = Utils.isLogWorking(Constants.ORIENTATION_LOG_FOLDER);
        String wifi = Utils.isLogWorking(Constants.WIFI_LOG_FOLDER);

        if (activity != null) {
            mActivityStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_ok));
            mActivitySample.setText(activity);
        } else {
            mActivityStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_error));
            mActivitySample.setText(getResources().getString(R.string.not_found));
        }

        if (audio != null) {
            mAudioStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_ok));
            mAudioSample.setText(audio);
        } else {
            mAudioStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_error));
            mAudioSample.setText(getResources().getString(R.string.not_found));
        }

        if (bluetooth != null) {
            mBluetoothStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_ok));
            mBluetoothSample.setText(bluetooth);
        } else {
            mBluetoothStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_error));
            mBluetoothSample.setText(getResources().getString(R.string.not_found));
        }

        if (orientation != null) {
            mOrientationStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_ok));
            mOrientationSample.setText(orientation);
        } else {
            mOrientationStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_error));
            mOrientationSample.setText(getResources().getString(R.string.not_found));
        }

        if (wifi != null) {
            mWifiStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_ok));
            mWifiSample.setText(wifi);
        } else {
            mWifiStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.status_error));
            mWifiSample.setText(getResources().getString(R.string.not_found));
        }

        mProgressBar.setVisibility(View.GONE);
    }
}
