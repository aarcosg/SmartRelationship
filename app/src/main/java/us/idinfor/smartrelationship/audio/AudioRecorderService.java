package us.idinfor.smartrelationship.audio;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;


public class AudioRecorderService extends IntentService {

    private static final String TAG = AudioRecorderService.class.getCanonicalName();

    public static void startActionRecordWav(Context context) {
        Intent intent = new Intent(context, AudioRecorderService.class);
        intent.setAction(Constants.ACTION_RECORD_WAV);
        context.startService(intent);
    }

    public AudioRecorderService() {
        super("AudioRecorderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_RECORD_WAV.equals(action)) {
                handleActionRecordWav();
            }
        }
    }

    private void handleActionRecordWav() {
        Log.i(TAG,"handleActionRecordWav");
        try{
            String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/" + Constants.ROOT_FOLDER
                    + "/" + Constants.AUDIO_RECORDER_FOLDER
                    + "/" + Utils.getDateTimeStamp()
                    + "_" + Utils.getSharedPreferences(this).getLong(Constants.PROPERTY_LISTENING_ID, 0L)
                    + Constants.AUDIO_RECORDER_WAV_EXT;
            final AudioRecorder recorder = new AudioRecorder(filepath);
            recorder.start();
            Log.i(TAG, "Start recorder");
            Timer myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(recorder.isRecording()){
                        Log.i(TAG, "Stop recorder");
                        recorder.stop();
                    }
                }
            }, 5000);

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
