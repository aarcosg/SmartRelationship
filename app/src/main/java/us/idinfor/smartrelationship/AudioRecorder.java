package us.idinfor.smartrelationship;


import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    final MediaRecorder recorder = new MediaRecorder();
    final String path;

    /**
     * Creates a new audio recording at the given path (relative to root of SD card).
     */
    public AudioRecorder(String path) {
        this.path = sanitizePath(path);
    }

    private String sanitizePath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.contains(".")) {
            path += ".mp3";
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + path;
    }

    /**
     * Starts a new recording.
     */
    public void start(){
        String state = android.os.Environment.getExternalStorageState();
        try {
            if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
                throw new IOException("SD Card is not mounted.  It is " + state + ".");
            }

            // make sure the directory we plan to store the recording in exists
            File directory = new File(path).getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Path to file could not be created.");
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(path);
            recorder.setMaxDuration(2000);
            recorder.prepare();
            recorder.start();
        }catch (IOException e){
            Log.e("Error",e.getMessage() );
            e.printStackTrace();
        }
    }

    /**
     * Stops a recording that has been previously started.
     */
    public void stop() {
        try {
            recorder.stop();
        } catch(RuntimeException e) {
            new File(path).delete();
        } finally {
            recorder.release();
        }
    }

}
