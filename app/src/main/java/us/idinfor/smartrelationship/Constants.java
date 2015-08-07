package us.idinfor.smartrelationship;

public class Constants {

    public static final String PACKAGE_NAME = "us.idinfor.smartrelationship";

    public static final Integer DEFAULT_SAMPLE_SCAN_FREQUENCY = 30;
    public static final Integer DEFAULT_VOICE_RECORD_DURATION = 5;

    public static final String PROPERTY_SAMPLE_SCAN_FREQUENCY = "sample_scan_frequency";
    public static final String PROPERTY_VOICE_RECORD_DURATION = "voice_record_duration";
    public static final String PROPERTY_LISTENING = "listening";
    public static final String PROPERTY_LISTENING_ID  = "listening_id";
    public static final String PROPERTY_LAST_ACTIVITIES_DETECTED = "last_activities_detected";
    public static final String PROPERTY_BLUETOOTH_LIST = "bluetooth_list";
    public static final String PROPERTY_ORIENTATION_AZIMUTH = "orientation_azimuth";
    public static final String PROPERTY_ORIENTATION_PITCH = "orientation_pitch";
    public static final String PROPERTY_ORIENTATION_ROLL = "orientation_roll";
    public static final String PROPERTY_TIMESTAMP = "timestamp";
    public static final String PROPERTY_RECORD_AUDIO_ENABLED = "record_audio_enabled";


    public static final String LOCK_BLUETOOTH_SCAN_SERVICE = PACKAGE_NAME + ".BluetoothScanBroadcastReceiver";
    public static final String LOCK_WIFI_SCAN_SERVICE = PACKAGE_NAME + ".OnWifiScanResultReceiver";
    public static final String LOCK_ALARM_SERVICE = PACKAGE_NAME + ".OnAlarmReceiver";
    public static final String LOCK_ACTIVITY_RECOGNITION_SERVICE = PACKAGE_NAME + ".OnActivityRecognitionResultReceiver";

    public static final String BLUETOOTH_LOG_FOLDER = "Bluetooth";
    public static final String WIFI_LOG_FOLDER = "Wifi";
    public static final String ACTIVITY_LOG_FOLDER = "Activity";
    public static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    public static final String ORIENTATION_LOG_FOLDER = "Orientation";
    public static final String AUDIO_RECORDER_MP3_EXT = ".mp3";
    public static final String AUDIO_RECORDER_WAV_EXT = ".wav";
    public static final String LOG_FILE_EXT = ".csv";
    public static final String ROOT_FOLDER = "SmartRelationship";

    public static final String CSV_SEPARATOR = ";";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST  = 9000;
    public static final long ACTIVITY_DETECTION_INTERVAL_IN_MILLISECONDS = 0;

    public static final String ACTION_START_LISTENING = PACKAGE_NAME + ".ACTION_START_LISTENING";
    public static final String ACTION_STOP_LISTENING = PACKAGE_NAME + ".ACTION_STOP_LISTENING";
    public static final String ACTION_ACTIVITY_RECOGNITION_RESULTS = PACKAGE_NAME + ".ACTION_ACTIVITY_RECOGNITION_RESULTS";
    public static final String ACTION_RECORD_WAV = PACKAGE_NAME + ".ACTION_RECORD_WAV";
    public static final String ACTION_SAMPLE_ORIENTATION = PACKAGE_NAME + ".ACTION_SAMPLE_ORIENTATION";
    public static final String ACTION_SAMPLE_WIFI = PACKAGE_NAME + ".ACTION_SAMPLE_WIFI";
    public static final String ACTION_WIFI_RESULTS = PACKAGE_NAME + ".ACTION_WIFI_RESULTS";
    public static final String ACTION_SAMPLE_BLUETOOTH = PACKAGE_NAME + ".ACTION_SAMPLE_BLUETOOTH";
    public static final String ACTION_SAMPLE_BLUETOOTH_FOUND = PACKAGE_NAME + ".ACTION_SAMPLE_BLUETOOTH_FOUND";
    public static final String ACTION_SAMPLE_BLUETOOTH_FINISHED = PACKAGE_NAME + ".ACTION_SAMPLE_BLUETOOTH_FINISHED";
    public static final String EXTRA_BLUETOOTH_DEVICE = PACKAGE_NAME + ".EXTRA_BLUETOOTH_DEVICE";
    public static final String ACTION_SAMPLE_ACTIVITY = PACKAGE_NAME + ".ACTION_SAMPLE_ACTIVITY";
    public static final String ACTION_SAMPLE_ACTIVITY_RESULT = PACKAGE_NAME + ".ACTION_SAMPLE_ACTIVITY_RESULT";
    public static final String ACTION_SAMPLE_ACTIVITY_SAVE = PACKAGE_NAME + ".ACTION_SAMPLE_ACTIVITY_SAVE";
    public static final String ACTION_SAMPLE_ACTIVITY_STOP = PACKAGE_NAME + ".ACTION_SAMPLE_ACTIVITY_STOP";

    public static final int INI_HOUR = 9;
    public static final int INI_MINUTE = 1;
    public static final int END_HOUR = 17;
    public static final int END_MINUTE = 1;

}