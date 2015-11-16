package us.idinfor.smartrelationship.zip;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;


public class ZipLogsIntentService extends IntentService {

    private static final String TAG = ZipLogsIntentService.class.getCanonicalName();

    public ZipLogsIntentService() {
        super("ZipLogsIntentService");
    }

    public static void startActionZipLogs(Context context) {
        Intent intent = new Intent(context, ZipLogsIntentService.class);
        intent.setAction(Constants.ACTION_AUTO_ZIP_LOGS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_AUTO_ZIP_LOGS.equals(action)) {
                handleActionZipLogs();
            }
        }
    }

    private void handleActionZipLogs() {
        Log.d(TAG, "Auto zipping logs");
        Utils.zipLogFiles();
    }
}
