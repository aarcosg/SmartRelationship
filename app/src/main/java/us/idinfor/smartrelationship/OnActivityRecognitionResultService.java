package us.idinfor.smartrelationship;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class OnActivityRecognitionResultService extends IntentService{

    private static final String TAG = OnActivityRecognitionResultService.class.getCanonicalName();
    private List<us.idinfor.smartrelationship.DetectedActivity> activities;

    public OnActivityRecognitionResultService(){
        super("OnActivityRecognitionResultService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.i(TAG, "OnActivityRecognitionResultService@onHandleIntent");
        if(ActivityRecognitionResult.hasResult(intent)){
            activities = new ArrayList<us.idinfor.smartrelationship.DetectedActivity>();
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            Log.i(TAG,result.toString());
            List<DetectedActivity> detectedActivities = result.getProbableActivities();
            // Log each activity.
            for (DetectedActivity da: detectedActivities) {
                Log.i(TAG, da.getType() + " " + da.getConfidence() + "%");
                us.idinfor.smartrelationship.DetectedActivity detectedActivity =
                        new us.idinfor.smartrelationship.DetectedActivity(Utils.getActivityRecognitionString(this,da.getType()),da.getConfidence());
                activities.add(detectedActivity);
            }
            Gson gson = new Gson();
            Utils.getSharedPreferences(this).edit().putString(Constants.PROPERTY_LAST_ACTIVITIES_DETECTED,gson.toJson(activities)).apply();
        }
    }
}
