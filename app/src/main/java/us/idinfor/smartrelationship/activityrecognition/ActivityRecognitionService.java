package us.idinfor.smartrelationship.activityrecognition;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;

public class ActivityRecognitionService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    private static final String TAG = ActivityRecognitionService.class.getCanonicalName();

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mActivityRecognitionPI;
    private boolean startRecognition;
    private boolean stopRecognition;

    public ActivityRecognitionService() {
    }

    public static void startActionSampleActivity(Context context) {
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        intent.setAction(Constants.ACTION_SAMPLE_ACTIVITY);
        context.startService(intent);
    }

    public static void startActionSampleActivitySave(Context context){
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        intent.setAction(Constants.ACTION_SAMPLE_ACTIVITY_SAVE);
        context.startService(intent);
    }

    public static void startActionSampleActivityStop(Context context){
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        intent.setAction(Constants.ACTION_SAMPLE_ACTIVITY_STOP);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.i(TAG,"onStartCommand" + intent.getAction());
            final String action = intent.getAction();
            if (Constants.ACTION_SAMPLE_ACTIVITY.equals(action)) {
                handleActionSampleActivity();
            }else if(Constants.ACTION_SAMPLE_ACTIVITY_RESULT.equals(action) && ActivityRecognitionResult.hasResult(intent)){
                handleActionSampleActivityResult(ActivityRecognitionResult.extractResult(intent));
            }else if(Constants.ACTION_SAMPLE_ACTIVITY_SAVE.equals(action)){
                handleActionSampleActivitySave();
            }else if(Constants.ACTION_SAMPLE_ACTIVITY_STOP.equals(action)){
                handleActionSampleActivityStop();
            }
        }
        return START_STICKY;
    }

    private void handleActionSampleActivity(){
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
    }

    private void handleActionSampleActivityResult(ActivityRecognitionResult result){
        List<DetectedActivity> activities = new ArrayList<DetectedActivity>();
        Log.i(TAG,result.toString());
        List<DetectedActivity> detectedActivities = result.getProbableActivities();
        for (DetectedActivity da: detectedActivities) {
            Log.i(TAG, Utils.getActivityRecognitionString(this, da.getType()) + " " + da.getConfidence() + "%");
            activities.add(da);
        }
        Utils.getSharedPreferences(this).edit().putString(Constants.PROPERTY_LAST_ACTIVITIES_DETECTED, new Gson().toJson(activities)).apply();
    }

    private void handleActionSampleActivitySave(){
        SharedPreferences prefs = Utils.getSharedPreferences(this);
        Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
        Long timestamp = prefs.getLong(Constants.PROPERTY_TIMESTAMP, 0L);
        Type listType = new TypeToken<ArrayList<DetectedActivity>>() {}.getType();
        List<DetectedActivity> activities = new Gson().fromJson(prefs.getString(Constants.PROPERTY_LAST_ACTIVITIES_DETECTED, ""), listType);
        int moving = 0;
        if(activities != null && !activities.isEmpty()){
            for(DetectedActivity da : activities) {
                switch (da.getType()) {
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.ON_BICYCLE:
                    case DetectedActivity.ON_FOOT:
                        moving += da.getConfidence();
                        break;
                }
            }

            Utils.writeToLogFile(Constants.ACTIVITY_LOG_FOLDER
                    ,timestamp + Constants.CSV_SEPARATOR
                    + listeningId + Constants.CSV_SEPARATOR
                    + (moving >= 50 ? 1 : 0));

        }else{
            Utils.writeToLogFile(Constants.ACTIVITY_LOG_FOLDER
                    ,timestamp + Constants.CSV_SEPARATOR
                    + listeningId + Constants.CSV_SEPARATOR
                    + Constants.CSV_SEPARATOR
                    + Constants.CSV_SEPARATOR);
        }
    }

    private void handleActionSampleActivityStop(){
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
            Intent intent = new Intent(this, ActivityRecognitionService.class);
            intent.setAction(Constants.ACTION_SAMPLE_ACTIVITY_RESULT);
            mActivityRecognitionPI = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mActivityRecognitionPI;
    }

    private void startActivityRecognition() {
        Log.i(TAG, "startActivityRecognition");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Utils.getSharedPreferences(this).getInt(Constants.PROPERTY_SAMPLE_SCAN_FREQUENCY, 30) / 2,
                getActivityRecognitionPendingIntent()
        );
    }

    private void stopActivityRecognition() {
        Log.i(TAG, "stoptActivityRecognition");
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityRecognitionPendingIntent()
        );
        stopSelf();
    }



}
