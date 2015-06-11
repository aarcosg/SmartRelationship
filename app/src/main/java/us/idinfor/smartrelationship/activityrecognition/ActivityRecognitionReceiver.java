package us.idinfor.smartrelationship.activityrecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.LogRecord;
import us.idinfor.smartrelationship.Utils;
import us.idinfor.smartrelationship.WakefulIntentService;

public class ActivityRecognitionReceiver extends BroadcastReceiver{

    private static final String TAG = ActivityRecognitionReceiver.class.getCanonicalName();

    private SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "ActivityRecognitionReceiver@onReceive");
        WakefulIntentService.acquireStaticLock(context, Constants.LOCK_ACTIVITY_RECOGNITION_SERVICE);
        prefs = Utils.getSharedPreferences(context);
        Gson gson = new Gson();
        Long listeningId = prefs.getLong(Constants.PROPERTY_LISTENING_ID, 0L);
        Type listType = new TypeToken<ArrayList<DetectedActivity>>() {}.getType();
        List<DetectedActivity> activities = new Gson().fromJson(prefs.getString(Constants.PROPERTY_LAST_ACTIVITIES_DETECTED,""),listType);
        LogRecord logRecord = new LogRecord(
                listeningId
                ,LogRecord.Type.ACTIVITY
                ,System.currentTimeMillis()
                ,null
                ,null
                ,activities
                ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_AZIMUTH, 0.0f)
                ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_PITCH, 0.0f)
                ,prefs.getFloat(Constants.PROPERTY_ORIENTATION_ROLL, 0.0f));
        Utils.writeToLogFile(Constants.ACTIVITY_LOG_FOLDER
                , Utils.getTimeStamp() + ";" + listeningId + ";" + gson.toJson(logRecord));


        
    }
}