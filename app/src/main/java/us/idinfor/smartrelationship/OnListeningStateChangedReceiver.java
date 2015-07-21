/***
 * Copyright (c) 2008-2012 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 * <p/>
 * From _The Busy Coder's Guide to Advanced Android Development_
 * http://commonsware.com/AdvAndroid
 */

package us.idinfor.smartrelationship;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import us.idinfor.smartrelationship.activityrecognition.OnActivityRecognitionResultService;
import us.idinfor.smartrelationship.audio.AudioRecorderService;
import us.idinfor.smartrelationship.bluetooth.BluetoothScanService;
import us.idinfor.smartrelationship.bluetooth.OnBluetoothScanResultReceiver;
import us.idinfor.smartrelationship.wifi.OnWifiScanResultReceiver;
import us.idinfor.smartrelationship.wifi.WifiScanService;

public class OnListeningStateChangedReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = OnListeningStateChangedReceiver.class.getCanonicalName();
    private static AlarmManager alarmManager;

    private static GoogleApiClient mGoogleApiClient;
    private static PendingIntent mActivityRecognitionPI;
    private static boolean startRecognition;
    private static boolean stopRecognition;

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"OnListeningStateChangedReceiver@onReceive");
        if(!isWeekend()){
            this.context = context;
            setBackgroundServicesState(context, Utils.getSharedPreferences(context).getBoolean(Constants.PROPERTY_LISTENING, true));
            alarmManager = getAlarmManagerInstance(context);
            Intent i = new Intent(context, OnAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
            if (TextUtils.equals(intent.getAction(), Constants.ACTION_START_LISTENING)) {
                Log.i(TAG, "Set Alarm Manager");
                setListeningState(context,true);
                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                if (mGoogleApiClient.isConnected()) {
                    startActivityRecognition();
                } else {
                    startRecognition = true;
                }
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 60000,
                        Utils.getSharedPreferences(context).getInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, Constants.DEFAULT_SAMPLE_SCAN_FREQUENCY) * 1000,
                        pi);
            } else if (TextUtils.equals(intent.getAction(), Constants.ACTION_STOP_LISTENING)) {
                Log.i(TAG, "Cancel Alarm Manager");
                setListeningState(context,false);
                if(mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                if (mGoogleApiClient.isConnected()) {
                    stopActivityRecognition();
                } else {
                    stopRecognition = true;
                }

                alarmManager.cancel(pi);
                Log.i(TAG, "Cancel Bluetooth Discovery");
                BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                if(mBluetoothManager.getAdapter() != null){
                    mBluetoothManager.getAdapter().cancelDiscovery();
                }
                Log.i(TAG,"Cancel Media Recorder");
                new MediaRecorder().release();
            }
        }else{
            Log.i(TAG,"It is weekend! I will not work until monday :)");
        }
    }

    private AlarmManager getAlarmManagerInstance(Context context) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        }
        return alarmManager;
    }

    private void setBackgroundServicesState(Context context, boolean listening){
        List<Class<?>> services = new ArrayList<Class<?>>();
        services.add(WifiScanService.class);
        services.add(OnWifiScanResultReceiver.class);
        services.add(BluetoothScanService.class);
        services.add(OnBluetoothScanResultReceiver.class);
        services.add(AudioRecorderService.class);

        int flag=(listening ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

        PackageManager pm = context.getPackageManager();
        for(Class<?> service : services){
            ComponentName component = new ComponentName(context,service);
            pm.setComponentEnabledSetting(component, flag, PackageManager.DONT_KILL_APP);
        }

    }

    private void setListeningState(Context context, boolean listening) {
        Utils.getSharedPreferences(context)
                .edit()
                .putBoolean(Constants.PROPERTY_LISTENING, listening)
                .apply();
    }



    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
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
        if(stopRecognition){
            stopActivityRecognition();
            stopRecognition = false;
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
            Intent intent = new Intent(context, OnActivityRecognitionResultService.class);
            mActivityRecognitionPI = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mActivityRecognitionPI;
    }

    private void startActivityRecognition() {
        Log.i(TAG, "startActivityRecognition");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Utils.getSharedPreferences(context).getInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, 30) / 2,
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

    private boolean isWeekend(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }
}