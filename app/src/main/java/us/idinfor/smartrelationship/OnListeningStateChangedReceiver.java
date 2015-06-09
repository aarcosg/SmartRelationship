/***
 * Copyright (c) 2008-2012 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 * <p/>
 * From _The Busy Coder's Guide to Advanced Android Development_
 * http://commonsware.com/AdvAndroid
 */

package us.idinfor.smartrelationship;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

public class OnListeningStateChangedReceiver extends BroadcastReceiver {

    private static final String TAG = OnListeningStateChangedReceiver.class.getCanonicalName();
    private static AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        alarmManager = getAlarmManagerInstance(context);
        Intent i = new Intent(context, OnAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        if (TextUtils.equals(intent.getAction(), Constants.START_LISTENING)) {
            Log.i(TAG, "Set Alarm Manager");
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 60000,
                    Utils.getSharedPreferences(context).getInt(Constants.PROPERTY_BLUETOOTH_SCAN_FRECUENCY, 10) * 1000,
                    pi);
        } else if (TextUtils.equals(intent.getAction(), Constants.STOP_LISTENING)) {
            Log.i(TAG, "Cancel Alarm Manager");
            alarmManager.cancel(pi);
        }

    }

    private AlarmManager getAlarmManagerInstance(Context context) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        }
        return alarmManager;
    }
}