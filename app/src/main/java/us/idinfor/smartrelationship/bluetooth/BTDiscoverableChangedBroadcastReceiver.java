package us.idinfor.smartrelationship.bluetooth;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.MainActivity;
import us.idinfor.smartrelationship.R;

public class BTDiscoverableChangedBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = BTDiscoverableChangedBroadcastReceiver.class.getCanonicalName();
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive : " + intent.getAction());
        if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(intent.getAction())) {
            Log.i(TAG, "Bluetooth discoverable mode changed");
            if(intent.getExtras() != null ){
                int currentMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                Log.i(TAG,"Current bluetooth discoverable mode = " + currentMode);
                if(currentMode != BluetoothAdapter.ERROR &&
                        (currentMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE
                                || currentMode == BluetoothAdapter.SCAN_MODE_NONE)){
                    Log.i(TAG,"Device is not discoverable. Show notification.");
                    showEnableDiscoverableBTNotification(context);
                }
            }
        }
    }

    private void showEnableDiscoverableBTNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constants.EXTRA_NOTIFICATION, NOTIFICATION_ID);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_error_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                    .setContentTitle(context.getResources().getString(R.string.notification_title))
                    .setContentText(context.getResources().getString(R.string.notification_description))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIFICATION_ID,builder.build());

    }
}