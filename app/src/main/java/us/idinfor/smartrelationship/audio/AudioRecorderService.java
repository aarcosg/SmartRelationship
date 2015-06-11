package us.idinfor.smartrelationship.audio;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import us.idinfor.smartrelationship.Constants;
import us.idinfor.smartrelationship.Utils;
import us.idinfor.smartrelationship.WakefulIntentService;

public class AudioRecorderService extends WakefulIntentService {

    private static final String TAG = AudioRecorderService.class.getCanonicalName();
    private static MediaRecorder recorder = new MediaRecorder();

    public AudioRecorderService() {
        super("AudioRecorderService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
        Log.i(TAG, "AudioRecorderService@doWakefulWork");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + Constants.ROOT_FOLDER
                + "/" + Constants.AUDIO_RECORDER_FOLDER
                + "/" + Utils.getDateTimeStamp()
                + "_" + Utils.getSharedPreferences(this).getLong(Constants.PROPERTY_LISTENING_ID, 0L)
                + Constants.AUDIO_RECORDER_FILE_EXT;
        File directory = new File(path).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Can't make audio recorder directory");
        }
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(path);
        recorder.setMaxDuration(Utils.getSharedPreferences(this).getInt(Constants.PROPERTY_VOICE_RECORD_DURATION, 5) * 1000);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        try {
            recorder.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}