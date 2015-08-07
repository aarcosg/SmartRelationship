package us.idinfor.smartrelationship.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioRecorder {

    private static final String TAG = AudioRecorder.class.getCanonicalName();

    private AudioRecord recorder;

    private String filepath;
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_TEMP_FILE = "temp_audio_record.raw";
    private static final int RECORDER_SAMPLERATE = 8000;//32000;//44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_INT = 1;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private int bufferSize = 200000;
    short[] buffer;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    public AudioRecorder(String outFilePath) {
        this.filepath = outFilePath;

        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        Log.d(TAG,"BUFFER SIZE VALUE IS " + bufferSize);

        int buffercount = 4088 / bufferSize;
        if (buffercount < 1) buffercount = 1;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, 44100);
    }

    public void start() throws IllegalStateException, IOException {
        buffer = new short[4088];
        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    public void stop() {
        Log.d(TAG,"Stop audio recording");
        stopRecording();
    }

    public boolean isRecording() {
        if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
            return true;
        else
            return false;
    }


    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
        // copy the recorded file to original copy & delete the recorded copy
        copyWaveFile(getTempFilename(), getFilename());
        deleteTempFile();
    }

    private String getFilename() {
        File directory = new File(filepath).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Can't make audio recorder directory");
        }
        return (filepath);
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }


    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = RECORDER_CHANNELS_INT;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            byte[] bytes2 = new byte[buffer.length * 2];
            ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer().put(buffer);
            while (in.read(bytes2) != -1) {
                out.write(bytes2);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTempFilename() {
        // Creates the temp file to store buffer
        File directory = new File(filepath).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Can't make audio recorder directory");
        }
        File tempFile = new File(directory, AUDIO_RECORDER_TEMP_FILE);
        return (directory + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;
        while (isRecording) {
            // gets the voice output from microphone to byte format
            read = recorder.read(buffer, 0, buffer.length);
            //read = recorder.read(data, 0, 6144);

            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                try {
                    // // writes the data to file from buffer
                    // // stores the voice buffer

                    // short[] shorts = new short[bytes.length/2];
                    // to turn bytes to shorts as either big endian or little
                    // endian.
                    // ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

                    // to turn shorts back to bytes.
                    byte[] bytes2 = new byte[buffer.length * 2];
                    ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN)
                            .asShortBuffer().put(buffer);

                    os.write(bytes2);
                    //  ServerInteractor.SendAudio(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate,
                                     int channels, long byteRate)throws IOException {
        byte[] header = new byte[4088];

        header[0] = 'R'; // RIFF/WAVE header
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
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) RECORDER_CHANNELS_INT;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (RECORDER_CHANNELS_INT * RECORDER_BPP / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 4088);
    }
}