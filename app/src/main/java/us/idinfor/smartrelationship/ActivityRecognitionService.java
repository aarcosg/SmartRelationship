package us.idinfor.smartrelationship;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

public class ActivityRecognitionService extends WakefulIntentService implements ResultCallback<Status>,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = ActivityRecognitionService.class.getCanonicalName();
    private static GoogleApiClient mGoogleApiClient;


    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Log.i(TAG,"ActivityRecognitionService@doWakefulWork");
        if(mGoogleApiClient == null){
            buildGoogleApiClient(this);
        }
        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }else{
            startActivityRecognition();
        }

    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient(Context context) {
        Log.d(TAG,"Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startActivityRecognition();
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

    @Override
    public void onResult(Status status) {
        if(status.isSuccess()){
            Log.i(TAG,"Activity updates added");
        }
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(Constants.ACTION_ACTIVITY_RECOGNITION_RESULTS);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*Intent intent = new Intent(this, OnActivityRecognitionResultReceiver.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);*/
    }

    private void startActivityRecognition(){
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.ACTIVITY_DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }


}