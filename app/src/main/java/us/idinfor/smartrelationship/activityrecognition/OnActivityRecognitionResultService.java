package us.idinfor.smartrelationship.activityrecognition;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;

public class OnActivityRecognitionResultService extends IntentService{

    private static final String TAG = OnActivityRecognitionResultService.class.getCanonicalName();
    private List<DetectedActivity> activities;

    public OnActivityRecognitionResultService(){
        super("OnActivityRecognitionResultService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.i(TAG, "OnActivityRecognitionResultService@onHandleIntent");
        if(ActivityRecognitionResult.hasResult(intent)){
            activities = new ArrayList<DetectedActivity>();
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            Log.i(TAG,result.toString());
            List<DetectedActivity> detectedActivities = result.getProbableActivities();
            // Log each activity.
            for (DetectedActivity da: detectedActivities) {
                Log.i(TAG, Utils.getActivityRecognitionString(this, da.getType()) + " " + da.getConfidence() + "%");
                /*us.idinfor.smartrelationship.activityrecognition.DetectedActivity detectedActivity =
                        new us.idinfor.smartrelationship.activityrecognition.DetectedActivity(Utils.getActivityRecognitionString(this,da.getType()),da.getConfidence());
                */
                activities.add(da);
            }
            Gson gson = new Gson();
            Utils.getSharedPreferences(this).edit().putString(Constants.PROPERTY_LAST_ACTIVITIES_DETECTED,gson.toJson(activities)).apply();
        }
    }
}
