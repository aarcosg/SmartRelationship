package us.idinfor.smartrelationship;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class OnBootReceiver extends BroadcastReceiver {

    private static final String TAG = OnBootReceiver.class.getCanonicalName();
    private static final int REQUEST_START_SAMPLING = 10;
    private static final int REQUEST_FINISH_SAMPLING = 11;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");

        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Calendar iniTime = Calendar.getInstance();
        iniTime.set(Calendar.HOUR_OF_DAY,Constants.INI_HOUR);
        iniTime.set(Calendar.MINUTE, Constants.INI_MINUTE);
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY,Constants.END_HOUR);
        endTime.set(Calendar.MINUTE, Constants.END_MINUTE);

        //Set initial alarm
        Intent startSamplingIntent = new Intent(context, OnListeningStateChangedReceiver.class);
        startSamplingIntent.setAction(Constants.ACTION_START_LISTENING);
        PendingIntent startSamplingPendingIntent = PendingIntent.getBroadcast(context, REQUEST_START_SAMPLING, startSamplingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, iniTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, startSamplingPendingIntent);
        Log.i(TAG, "Set start sampling alarm");

        //Set end alarm
        Intent stopSamplingIntent = new Intent(context, OnListeningStateChangedReceiver.class);
        stopSamplingIntent.setAction(Constants.ACTION_STOP_LISTENING);
        PendingIntent stopSamplingPendingIntent = PendingIntent.getBroadcast(context, REQUEST_FINISH_SAMPLING, stopSamplingIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(stopSamplingPendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, endTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, stopSamplingPendingIntent);
        Log.i(TAG, "Set finish sampling alarm");

        //Start sampling process if current time is between initial time and end time
        Calendar currentTime = Calendar.getInstance();
        if(currentTime.after(iniTime) && currentTime.before(endTime)){
            Log.i(TAG,"Device booted between inital time and end time");
            context.sendBroadcast(new Intent(Constants.ACTION_START_LISTENING));
        }



    }
}
