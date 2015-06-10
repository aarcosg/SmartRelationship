package us.idinfor.smartrelationship;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.location.DetectedActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static final String TAG = Utils.class.getCanonicalName();

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    synchronized public static void writeToLogFile(String folder, String msg){
        File log = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + Constants.ROOT_FOLDER
                + "/" + folder
                + "/" + Utils.getDateStamp()+ "_" + folder + Constants.LOG_FILE_EXT);
        File directory = log.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG,"Can't make " + folder + " directory");
        }

        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(log.getAbsolutePath(),log.exists()));
            out.write(msg);
            out.write("\n");
            out.close();
        }catch (IOException e){
            Log.e(TAG,"Exception appending to log file",e);
        }
    }

    public static String getTimeStamp(){
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String getDateStamp(){
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static String getDateTimeStamp(){
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    public static String getActivityRecognitionString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            default:
                return resources.getString(R.string.unknown);
        }
    }
}
