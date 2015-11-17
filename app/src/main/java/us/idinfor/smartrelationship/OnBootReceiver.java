package us.idinfor.smartrelationship;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;

import us.idinfor.smartrelationship.zip.ZipLogsIntentService;

public class OnBootReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = OnBootReceiver.class.getCanonicalName();
    private static final int REQUEST_START_SAMPLING = 10;
    private static final int REQUEST_FINISH_SAMPLING = 11;
    private static final int REQUEST_ZIP_LOGS = 12;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Calendar iniTime = Calendar.getInstance();
            iniTime.setTimeInMillis(System.currentTimeMillis());
            iniTime.set(Calendar.HOUR_OF_DAY, Constants.INI_HOUR);
            iniTime.set(Calendar.MINUTE, Constants.INI_MINUTE);

            //Set start sampling alarm
            Intent startSamplingIntent = new Intent(context, OnListeningStateChangedReceiver.class);
            startSamplingIntent.setAction(Constants.ACTION_START_LISTENING);
            PendingIntent startSamplingPendingIntent = PendingIntent.getBroadcast(context, REQUEST_START_SAMPLING, startSamplingIntent, 0);
            alarmManager.cancel(startSamplingPendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, iniTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startSamplingPendingIntent);
            Log.i(TAG, "Start sampling alarm set");

            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(System.currentTimeMillis());
            endTime.set(Calendar.HOUR_OF_DAY, Constants.END_HOUR);
            endTime.set(Calendar.MINUTE, Constants.END_MINUTE);

            //Set stop sampling alarm
            Intent stopSamplingIntent = new Intent(context, OnListeningStateChangedReceiver.class);
            stopSamplingIntent.setAction(Constants.ACTION_STOP_LISTENING);
            PendingIntent stopSamplingPendingIntent = PendingIntent.getBroadcast(context, REQUEST_FINISH_SAMPLING, stopSamplingIntent, 0);
            alarmManager.cancel(stopSamplingPendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, endTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, stopSamplingPendingIntent);
            Log.i(TAG, "Stop sampling alarm set");

            //Start sampling process if current time is between initial time and end time
            Calendar currentTime = Calendar.getInstance();
            if(currentTime.after(iniTime) && currentTime.before(endTime)){
                Log.i(TAG,"Device booted between inital time and end time");
                context.sendBroadcast(new Intent(Constants.ACTION_START_LISTENING));
            }

            // Set auto zip logs alarm
            currentTime.set(Calendar.HOUR_OF_DAY,Constants.AUTO_ZIP_HOUR);
            currentTime.set(Calendar.MINUTE, Constants.AUTO_ZIP_MINUTE);

            Intent zipLogsIntent = new Intent(context, ZipLogsIntentService.class);
            zipLogsIntent.setAction(Constants.ACTION_AUTO_ZIP_LOGS);
            PendingIntent zipLogsPendingIntent = PendingIntent.getService(context, REQUEST_ZIP_LOGS, zipLogsIntent, 0);
            alarmManager.cancel(zipLogsPendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, currentTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, zipLogsPendingIntent);
            Log.i(TAG, "Auto zip logs alarm set");
        }
    }
}
