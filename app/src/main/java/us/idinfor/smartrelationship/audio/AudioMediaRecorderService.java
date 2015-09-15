package us.idinfor.smartrelationship.audio;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;

public class AudioMediaRecorderService extends IntentService{

    private static final String TAG = AudioMediaRecorderService.class.getCanonicalName();
    public static MediaRecorder recorder;

    public AudioMediaRecorderService() {
        super("AudioMediaRecorderService");
    }

    public static void startActionMediaRecordMp3(Context context) {
        Intent intent = new Intent(context, AudioMediaRecorderService.class);
        intent.setAction(Constants.ACTION_MEDIA_RECORD_MP3);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_MEDIA_RECORD_MP3.equals(action)) {
                handleActionMediaRecordMP3();
            }
        }
    }

    private void handleActionMediaRecordMP3() {
        Log.i(TAG,"Recording audio @ handleActionMediaRecordMP3");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + Constants.ROOT_FOLDER
                + "/" + Constants.AUDIO_RECORDER_FOLDER
                + "/" + Utils.getDateTimeStamp()
                + "_" + Utils.getSharedPreferences(this).getLong(Constants.PROPERTY_LISTENING_ID, 0L)
                + Constants.AUDIO_RECORDER_MP3_EXT;
        File directory = new File(path).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Can't make audio recorder directory");
        }
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(path);
        recorder.setMaxDuration(Utils.getSharedPreferences(this).getInt(Constants.PROPERTY_VOICE_RECORD_DURATION, Constants.DEFAULT_VOICE_RECORD_DURATION) * 1000);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            //Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
