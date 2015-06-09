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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class OnAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = OnAlarmReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"OnAlarmReceiver@onReceive");
        WakefulIntentService.acquireStaticLock(context);
        SharedPreferences prefs = Utils.getSharedPreferences(context);
        prefs.edit().putLong(Constants.PROPERTY_LISTENING_ID,prefs.getLong(Constants.PROPERTY_LISTENING_ID,0L)+1L).commit();
        //context.startService(new Intent(context, AudioRecorderService.class));
       // context.startService(new Intent(context, BluetoothScanService.class));
        context.startService(new Intent(context, WifiScanService.class));
    }
}