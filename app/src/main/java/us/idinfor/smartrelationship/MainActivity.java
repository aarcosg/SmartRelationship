package us.idinfor.smartrelationship;

import android.content.Intent;
import android.content.SharedPreferences;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = Utils.getSharedPreferences(this);
        ButterKnife.inject(this);
        buildActionBarToolbar(getString(R.string.app_name), false);
        setButtonsEnabledState();
    }

    @OnClick(R.id.start_listening_btn)
    public void startListening() {
        Intent intent = new Intent();
        intent.setAction(Constants.START_LISTENING);
        sendBroadcast(intent);
        setListeningState(true);
        setButtonsEnabledState();
    }

    @OnClick(R.id.stop_listening_btn)
    public void stopListening() {
        Intent intent = new Intent();
        intent.setAction(Constants.STOP_LISTENING);
        sendBroadcast(intent);
        setListeningState(false);
        setButtonsEnabledState();
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
                    .make(view, "Datos guardados", Snackbar.LENGTH_LONG).show();
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
     * Retrieves the boolean from SharedPreferences that tracks whether we are listening to
     * bluetooth devices
     */
    private boolean getListeningState() {
        return Utils.getSharedPreferences(this)
                .getBoolean(Constants.PROPERTY_LISTENING, false);
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether we are listening to
     * bluetooth devices
     */
    private void setListeningState(boolean listening) {
        Utils.getSharedPreferences(this)
                .edit()
                .putBoolean(Constants.PROPERTY_LISTENING, listening)
                .apply();
    }


}
