package us.idinfor.smartrelationship;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class OnActivityRecognitionResultReceiver extends BroadcastReceiver implements ResultCallback<Status>,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener  {

    private static final String TAG = OnActivityRecognitionResultReceiver.class.getCanonicalName();
    private static GoogleApiClient mGoogleApiClient;
    private boolean mTaskFinished = false;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnActivityRecognitionResultReceiver@onReceive");
        WakefulIntentService.acquireStaticLock(context,Constants.LOCK_ACTIVITY_RECOGNITION_SERVICE);
        mContext = context;
        if(ActivityRecognitionResult.hasResult(intent)){
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            Log.d(TAG, "ActivityRecognitionResult = " + result.toString());
            List<DetectedActivity> detectedActivities = result.getProbableActivities();
            // Log each activity.
            Log.i(TAG, "activities detected");
            for (DetectedActivity da: detectedActivities) {
                Log.i(TAG, da.getType() + " " + da.getConfidence() + "%");
            }
            mTaskFinished = true;
            if(mGoogleApiClient == null){
                buildGoogleApiClient();
            }
            if(!mGoogleApiClient.isConnected()){
                mGoogleApiClient.connect();
            }else{
                stopActivityRecognition();
            }

        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG,"Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(Constants.ACTION_ACTIVITY_RECOGNITION_RESULTS);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(mTaskFinished){
            stopActivityRecognition();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Google Play Services connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Google Play Services connection failed");
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, "Activity updates removed");
        }
    }

    private void stopActivityRecognition(){
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }
}
