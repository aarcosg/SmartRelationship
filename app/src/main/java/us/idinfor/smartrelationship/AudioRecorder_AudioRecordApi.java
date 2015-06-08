package us.idinfor.smartrelationship;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorder_AudioRecordApi {

    private static final String TAG = AudioRecorder_AudioRecordApi.class.getCanonicalName();

    public static final int RECORDER_BPP = 16;
    public static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    public static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    public static final int BytesPerElement = 8; // 2 bytes in 16bit format
    private static AudioRecorder_AudioRecordApi instance = null;
    private int bufferSize;
    private AudioRecord recorder;

    public static final int RECORDER_SAMPLERATE_CD = 44100;
    public static final int RECORDER_SAMPLERATE_8000 = 8000;
    private AsyncTask task;
    /**
     * state variable to control starting and stopping recording
     */
    public boolean continueRecording;

    private static final int DEFAULT_BUFFER_INCREASE_FACTOR = 3;

    public AudioRecorder_AudioRecordApi(){
       /* bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC
                ,RECORDER_SAMPLERATE
                ,RECORDER_CHANNELS
                ,RECORDER_AUDIO_ENCODING
                ,BufferElements2Rec * BytesPerElement);*/
    }

    public static AudioRecorder_AudioRecordApi getInstance(){
        if(instance == null){
            instance = new AudioRecorder_AudioRecordApi();
        }
        return instance;
    }

    public AudioRecord getRecorder(){
        return recorder;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * start recording: set the parameters that correspond to a buffer that
     * contains millisecondsPerAudioClip milliseconds of samples
     */
    public boolean startRecordingForTime(int millisecondsPerAudioClip,
                                         int sampleRate, int encoding)
    {
        float percentOfASecond = (float) millisecondsPerAudioClip / 1000.0f;
        int numSamplesRequired = (int) ((float) sampleRate * percentOfASecond);
        int bufferSize =
                determineCalculatedBufferSize(sampleRate, encoding,
                        numSamplesRequired);
        this.bufferSize = bufferSize;

        return doRecording(sampleRate, encoding, bufferSize,
                numSamplesRequired, DEFAULT_BUFFER_INCREASE_FACTOR);
    }

    /**
     * start recording: Use a minimum audio buffer and a read buffer of the same
     * size.
     */
    public boolean startRecording(final int sampleRate, int encoding)
    {
        int bufferSize = determineMinimumBufferSize(sampleRate, encoding);
        return doRecording(sampleRate, encoding, bufferSize, bufferSize,
                DEFAULT_BUFFER_INCREASE_FACTOR);
    }

    private int determineMinimumBufferSize(final int sampleRate, int encoding)
    {
        int minBufferSize =
                AudioRecord.getMinBufferSize(sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, encoding);
        return minBufferSize;
    }

    /**
     * Calculate audio buffer size such that it holds numSamplesInBuffer and is
     * bigger than the minimum size<br>
     *
     * @param numSamplesInBuffer
     *            Make the audio buffer size big enough to hold this many
     *            samples
     */
    private int determineCalculatedBufferSize(final int sampleRate,
                                              int encoding, int numSamplesInBuffer)
    {
        int minBufferSize = determineMinimumBufferSize(sampleRate, encoding);

        int bufferSize;
        // each sample takes two bytes, need a bigger buffer
        if (encoding == AudioFormat.ENCODING_PCM_16BIT)
        {
            bufferSize = numSamplesInBuffer * 2;
        }
        else
        {
            bufferSize = numSamplesInBuffer;
        }

        if (bufferSize < minBufferSize)
        {
            Log.w(TAG, "Increasing buffer to hold enough samples "
                    + minBufferSize + " was: " + bufferSize);
            bufferSize = minBufferSize;
        }

        return bufferSize;
    }

    /**
     * Records audio until stopped the {@link #task} is canceled,
     * {@link #continueRecording} is false, or {@link #clipListener} returns
     * true <br>
     * records audio to a short [readBufferSize] and passes it to
     * {@link #clipListener} <br>
     * uses an audio buffer of size bufferSize * bufferIncreaseFactor
     *
     * @param recordingBufferSize
     *            minimum audio buffer size
     * @param readBufferSize
     *            reads a buffer of this size
     * @param bufferIncreaseFactor
     *            to increase recording buffer size beyond the minimum needed
     */
    private boolean doRecording(final int sampleRate, int encoding,
                                int recordingBufferSize, int readBufferSize,
                                int bufferIncreaseFactor)
    {
        if (recordingBufferSize == AudioRecord.ERROR_BAD_VALUE)
        {
            Log.e(TAG, "Bad encoding value, see logcat");
            return false;
        }
        else if (recordingBufferSize == AudioRecord.ERROR)
        {
            Log.e(TAG, "Error creating buffer size");
            return false;
        }

        // give it extra space to prevent overflow
        int increasedRecordingBufferSize =
                recordingBufferSize * bufferIncreaseFactor;

        recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, encoding,
                        increasedRecordingBufferSize);

        final short[] readBuffer = new short[readBufferSize];

        continueRecording = true;
        Log.d(TAG, "start recording, " + "recording bufferSize: "
                + increasedRecordingBufferSize
                + " read buffer size: " + readBufferSize);

        //Note: possible IllegalStateException
        //if audio recording is already recording or otherwise not available
        //AudioRecord.getState() will be AudioRecord.STATE_UNINITIALIZED
        recorder.startRecording();
        String filename = getTempFilename();
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (continueRecording)
        {
            int bufferResult = recorder.read(readBuffer, 0, readBufferSize);
            //in case external code stopped this while read was happening
            if ((!continueRecording) || ((task != null) && task.isCancelled()))
            {
                break;
            }
            // check for error conditions
            if (bufferResult == AudioRecord.ERROR_INVALID_OPERATION)
            {
                Log.e(TAG, "error reading: ERROR_INVALID_OPERATION");
            }
            else if (bufferResult == AudioRecord.ERROR_BAD_VALUE)
            {
                Log.e(TAG, "error reading: ERROR_BAD_VALUE");
            }
            else
            // no errors, do processing
            {
                try {
                    Log.d(TAG,"bufferResult = "+bufferResult);
                    // // writes the data to file from buffer
                    // // stores the voice buffer
                    byte bData[] = short2byte(readBuffer);
                    os.write(bData, 0, readBufferSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e(TAG,"NO errors, do processing");
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopRecording();
        copyWaveFile(getTempFilename(), getFilename());
        deleteTempFile();
        done();

        return true;
    }

    public boolean isRecording()
    {
        return continueRecording;
    }

    public void stopRecording()
    {
        continueRecording = false;
    }
    /**
     * need to call this when completely done with recording
     */
    public void done()
    {
        Log.d(TAG, "shut down recorder");
        if (recorder != null)
        {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        Log.e(TAG,"writeaudiodatatofile");
        String filename = getTempFilename();

        short sData[] = new short[AudioRecorder_AudioRecordApi.BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //while (prefs.getBoolean(Constants.PROPERTY_AUDIO_IS_RECORDING,false)) {
        // gets the voice output from microphone to byte format

        getRecorder().read(sData, 0, AudioRecorder_AudioRecordApi.BufferElements2Rec);
        //Log.e(TAG,"Short wirting to file" + sData.toString());
        try {
            // // writes the data to file from buffer
            // // stores the voice buffer
            byte bData[] = short2byte(sData);
            os.write(bData, 0, AudioRecorder_AudioRecordApi.BufferElements2Rec * AudioRecorder_AudioRecordApi.BytesPerElement);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG,"writeaudiodatatofile fin");
    }

    private void copyWaveFile(String inFilename,String outFilename){
        Log.e(TAG,"copyWaveFile");
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AudioRecorder_AudioRecordApi.RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = AudioRecorder_AudioRecordApi.RECORDER_BPP * AudioRecorder_AudioRecordApi.RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[this.bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.e(TAG,"File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = AudioRecorder_AudioRecordApi.RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AudioRecorder_AudioRecordApi.AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav");
    }

    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AudioRecorder_AudioRecordApi.AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,"record_temp.raw");

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + "record_temp.raw");
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }
}
