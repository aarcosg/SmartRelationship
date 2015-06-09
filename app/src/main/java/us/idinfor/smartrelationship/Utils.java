package us.idinfor.smartrelationship;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    private static final String TAG = Utils.class.getCanonicalName();

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    synchronized public static void writeToLogFile(String msg){
        File log = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + Constants.AUDIO_RECORDER_FOLDER + "/log.txt");
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(log.getAbsolutePath(),log.exists()));
            out.write(msg);
            out.write("\n");
            out.close();
        }catch (IOException e){
            Log.e(TAG,"Exception appending to log file",e);
        }

    }


}
