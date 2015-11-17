package us.idinfor.smartrelationship;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.location.DetectedActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    private static final String TAG = Utils.class.getCanonicalName();

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    synchronized public static void writeToLogFile(String folder, String msg) {
        File log = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + Constants.ROOT_FOLDER
                + "/" + folder
                + "/" + Utils.getDateStamp() + "_" + folder + Constants.LOG_FILE_EXT);
        File directory = log.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Can't make " + folder + " directory");
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(log.getAbsolutePath(), log.exists()));
            out.write(msg);
            out.write("\n");
            out.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception appending to log file", e);
        }
    }

    public static String isLogWorking(String folder) {
        String res = null;
        if (folder.equals(Constants.AUDIO_RECORDER_FOLDER)) {
            File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/" + Constants.ROOT_FOLDER
                    + "/" + Constants.AUDIO_RECORDER_FOLDER);
            if(audioDir.exists() && audioDir.isDirectory()){
                File lastFile = null;
                long lastModified = Long.MIN_VALUE;
                for(File f : audioDir.listFiles()){
                    if(f.lastModified() > lastModified){
                        lastModified = f.lastModified();
                        lastFile = f;
                    }
                }
                Long now = Calendar.getInstance().getTimeInMillis();
                if(lastFile != null && now - lastFile.lastModified() <= 5*60*1000){
                    res = lastFile.getName();
                }
            }
        } else {
            File log = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/" + Constants.ROOT_FOLDER
                    + "/" + folder
                    + "/" + Utils.getDateStamp() + "_" + folder + Constants.LOG_FILE_EXT);
            if (log.exists()) {
                String lastLine = fileTail(log, 1);
                Long logTime =  Long.valueOf(TextUtils.split(lastLine, ";")[0]);
                Long now = Calendar.getInstance().getTimeInMillis();
                if (now - logTime <= 5*60*1000){
                    res = lastLine;
                }
            }
        }
        return res;
    }

    public static String getTimeStamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static String getDateStamp() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static String getDateTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    public static String zipLogFiles() {
        Log.i(TAG, "Start zipping log files");
        File rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + Constants.ROOT_FOLDER);
        File zipFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.ROOT_FOLDER + getDateTimeStamp() + ".zip");
        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);

        try {
            FileOutputStream fout = new FileOutputStream(zipFile);
            ZipOutputStream zout = new ZipOutputStream(fout);
            addDirectory(zout, rootDir, rootDir, currentDate.getTimeInMillis());
            zout.close();
        } catch (IOException ioe) {
            Log.e(TAG, "IOException :" + ioe);
        }
        Log.i(TAG, "Logs files zipped successfully");
        return zipFile.getAbsolutePath();
    }

    private static void addDirectory(ZipOutputStream zout, File fileSource, File rootDir, Long currentDateInMillis) {

        //get sub-folder/files list
        File[] files = fileSource.listFiles();
        for (File file : files) {
            //if the file is directory, call the function recursively
            if (file.isDirectory()) {
                addDirectory(zout, file, rootDir, currentDateInMillis);
                continue;
            }
            try {
                if (file.lastModified() < currentDateInMillis) {
                    byte[] buffer = new byte[1024];
                    FileInputStream fin = new FileInputStream(file);
                    zout.putNextEntry(new ZipEntry(rootDir.getName() + "/" + fileSource.getName() + "/" + file.getName()));
                    int length;
                    while ((length = fin.read(buffer)) > 0) {
                        zout.write(buffer, 0, length);
                    }
                    zout.closeEntry();
                    fin.close();
                    file.delete();
                }
            } catch (IOException ioe) {
                Log.e(TAG, "IOException :" + ioe);
            }
        }
    }

    public static boolean isWeekend() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }


    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    public static String getActivityRecognitionString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch (detectedActivityType) {
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

    /**
     * Returns a human readable String corresponding to a major bluetooth class.
     */
    public static String getMajorBluetoothClassString(Context context, int majorBTClass) {
        Resources resources = context.getResources();
        switch (majorBTClass) {
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return resources.getString(R.string.audio_video);
            case BluetoothClass.Device.Major.COMPUTER:
                return resources.getString(R.string.computer);
            case BluetoothClass.Device.Major.HEALTH:
                return resources.getString(R.string.health);
            case BluetoothClass.Device.Major.IMAGING:
                return resources.getString(R.string.imaging);
            case BluetoothClass.Device.Major.MISC:
                return resources.getString(R.string.misc);
            case BluetoothClass.Device.Major.NETWORKING:
                return resources.getString(R.string.networking);
            case BluetoothClass.Device.Major.PERIPHERAL:
                return resources.getString(R.string.peripheral);
            case BluetoothClass.Device.Major.PHONE:
                return resources.getString(R.string.phone);
            case BluetoothClass.Device.Major.TOY:
                return resources.getString(R.string.toy);
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return resources.getString(R.string.uncategorized);
            case BluetoothClass.Device.Major.WEARABLE:
                return resources.getString(R.string.wearable);
            default:
                return resources.getString(R.string.uncategorized);
        }
    }

    //http://stackoverflow.com/a/7322581
    private static String fileTail(File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler =
                    new java.io.RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer < fileLength) {
                        line = line + 1;
                    }
                } else if (readByte == 0xD) {
                    if (filePointer < fileLength - 1) {
                        line = line + 1;
                    }
                }
                if (line >= lines) {
                    break;
                }
                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                }
        }
    }
}
