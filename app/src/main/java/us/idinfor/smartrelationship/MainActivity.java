package us.idinfor.smartrelationship;

import android.app.Activity;
import android.app.PendingIntent;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import us.idinfor.smartrelationship.activityrecognition.OnActivityRecognitionResultService;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DISCOVERABLE_BT = 2;

    @InjectView(R.id.sample_scan_frequency_edit)
    EditText mSampleScanfrequencyEdit;
    @InjectView(R.id.sample_scan_frequency_til)
    TextInputLayout mSampleScanfrequencyTil;
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
    BluetoothAdapter mBluetoothAdapter;
    GoogleApiClient mGoogleApiClient;
    PendingIntent mActivityRecognitionPI;
    boolean startRecognition;

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
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }

    @OnClick(R.id.start_listening_btn)
    public void startListening() {
        checkBluetooth();
        if (bluetoothEnabled) {
            setListeningState(true);
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_START_LISTENING);
            sendBroadcast(intent);
            setButtonsEnabledState();
            if (mGoogleApiClient.isConnected()) {
                startActivityRecognition();
            } else {
                startRecognition = true;
            }
        }
    }

    @OnClick(R.id.stop_listening_btn)
    public void stopListening() {
        setListeningState(false);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_STOP_LISTENING);
        sendBroadcast(intent);
        setButtonsEnabledState();
        if (mGoogleApiClient.isConnected()) {
            stopActivityRecognition();
        }
    }

    @OnClick(R.id.save_btn)
    public void savePreferences(final View view) {
        // Reset errors
        mSampleScanfrequencyTil.setError(null);
        mVoiceRecordDurationTil.setError(null);

        String scanfrequency = mSampleScanfrequencyEdit.getText().toString();
        String recordDuration = mVoiceRecordDurationEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check preferences
        if (TextUtils.isEmpty(scanfrequency) || (!TextUtils.isEmpty(scanfrequency) && Integer.valueOf(scanfrequency) <= 0)) {
            mSampleScanfrequencyTil.setError(getString(R.string.error_scan_frequency));
            cancel = true;
            focusView = mSampleScanfrequencyEdit;
        } else if (TextUtils.isEmpty(recordDuration) || (!TextUtils.isEmpty(recordDuration) && Integer.valueOf(recordDuration) <= 0)) {
            mVoiceRecordDurationTil.setError(getString(R.string.error_record_duration));
            cancel = true;
            focusView = mVoiceRecordDurationEdit;
        } else if (Integer.valueOf(scanfrequency) <= Integer.valueOf(recordDuration)) {
            mSampleScanfrequencyTil.setError(getString(R.string.error_scan_freq_lower_record_duration));
            cancel = true;
            focusView = mSampleScanfrequencyEdit;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Utils.hideSoftKeyboard(this);
            prefs.edit()
                    .putInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, Integer.valueOf(scanfrequency))
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
            mSampleScanfrequencyEdit.setEnabled(false);
            mVoiceRecordDurationEdit.setEnabled(false);
            mSaveBtn.setEnabled(false);
        } else {
            mStartListeningBtn.setEnabled(true);
            mStopListeningBtn.setEnabled(false);
            mSampleScanfrequencyEdit.setEnabled(true);
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
            /*bluetoothEnabled = true;
            startListening();
            return;*/
            checkBluetoothDiscoverable();
        } else if (requestCode == REQUEST_ENABLE_DISCOVERABLE_BT && resultCode == Activity.RESULT_OK) {
            bluetoothEnabled = true;
            startListening();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadPreferences() {
        mSampleScanfrequencyEdit.setText(Integer.valueOf(prefs.getInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, 30)).toString());
        mVoiceRecordDurationEdit.setText(Integer.valueOf(prefs.getInt(Constants.PROPERTY_VOICE_RECORD_DURATION, 5)).toString());
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

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Google Play Services connected");
        if (startRecognition) {
            startActivityRecognition();
            startRecognition = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.w(TAG, "Google Play Services connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Google Play Services connection failed");
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityRecognitionPendingIntent() {
        if (mActivityRecognitionPI == null) {
            Intent intent = new Intent(this,OnActivityRecognitionResultService.class);
            mActivityRecognitionPI = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mActivityRecognitionPI;
    }

    private void startActivityRecognition() {
        Log.i(TAG, "startActivityRecognition");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                prefs.getInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, 30) / 2,
                getActivityRecognitionPendingIntent()
        );

    }

    private void stopActivityRecognition() {
        Log.i(TAG, "stoptActivityRecognition");
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityRecognitionPendingIntent()
        );

    }
}
