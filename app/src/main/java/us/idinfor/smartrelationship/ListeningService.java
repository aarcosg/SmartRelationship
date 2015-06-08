package us.idinfor.smartrelationship;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class ListeningService extends WakefulIntentService {

    private static final String TAG = ListeningService.class.getCanonicalName();
    private static MediaRecorder recorder = new MediaRecorder();
    public ListeningService() {
        super("ListeningService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if(recorder == null){
            recorder = new MediaRecorder();
        }
        Log.e(TAG, "@doWakefulWork");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioRecorder/"+ System.currentTimeMillis() + ".mp3";
        // make sure the directory we plan to store the recording in exists
        File directory = new File(path).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
           Log.e(TAG,"Can't create directory");
        }
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(path);
        recorder.setMaxDuration(Utils.getSharedPreferences(this).getInt(Constants.PROPERTY_VOICE_RECORD_DURATION,5)*1000);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
        try{
            recorder.start();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }

        /*AudioRecorder audioRecorder = new AudioRecorder("/AudioRecorder/"+ System.currentTimeMillis() + ".mp3");
        audioRecorder.start();*/
        //audioRecorder = new AudioRecorder_AudioRecordApi();
//        Log.e(TAG,audioRecorder.getRecorder().getRecordingState()+"");
        //audioRecorder.startRecordingForTime(3000,AudioRecorder.RECORDER_SAMPLERATE_8000, AudioFormat.ENCODING_PCM_16BIT);
        //audioRecorder.getRecorder().startRecording();
        /*Log.e(TAG,audioRecorder.getRecorder().getRecordingState()+"");
        prefs.edit().putBoolean(Constants.PROPERTY_AUDIO_IS_RECORDING,true).commit();
        writeAudioDataToFile();
        prefs.edit().putBoolean(Constants.PROPERTY_AUDIO_IS_RECORDING, false).commit();
        audioRecorder.getRecorder().stop();
        audioRecorder.getRecorder().release();

        handler = null;
        copyWaveFile(getTempFilename(), getFilename());
        deleteTempFile();
        Log.e(TAG,audioRecorder.getRecorder().getRecordingState()+"");*/

        /*handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Stop recording on finish");
                if (audioRecorder != null) {
                    prefs.edit().putBoolean(Constants.PROPERTY_AUDIO_IS_RECORDING, false).commit();
                    audioRecorder.getRecorder().stop();
                    audioRecorder.getRecorder().release();
                    handler = null;
                    copyWaveFile(getTempFilename(), getFilename());
                    deleteTempFile();
                }
            }
        }, 5000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e(TAG,"Code inside timer");
            }
        }, 2000);*/





        /*File log = new File(Environment.getExternalStorageDirectory(),
                "AlarmLog.txt");

        try {
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(log.getAbsolutePath(),
                            log.exists()));

            out.write(new Date().toString());
            out.write("\n");
            out.close();
        } catch (IOException e) {
            Log.e("AppService", "Exception appending to log file", e);
        }*/
    }




}