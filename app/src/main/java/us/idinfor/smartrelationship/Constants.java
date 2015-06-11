package us.idinfor.smartrelationship;

public class Constants {

    public static final String PACKAGE_NAME = "us.idinfor.smartrelationship";
    public static final String PROPERTY_SAMPLE_SCAN_FREQUENCY = "sample_scan_frequency";
    public static final String PROPERTY_VOICE_RECORD_DURATION = "voice_record_duration";
    public static final String PROPERTY_LISTENING = "listening";
    public static final String PROPERTY_LISTENING_ID  = "listening_id";
    public static final String PROPERTY_LAST_ACTIVITIES_DETECTED = "last_activities_detected";
    public static final String PROPERTY_ORIENTATION_AZIMUTH = "orientation_azimuth";
    public static final String PROPERTY_ORIENTATION_PITCH = "orientation_pitch";
    public static final String PROPERTY_ORIENTATION_ROLL = "orientation_roll";
    public static final String ACTION_START_LISTENING = PACKAGE_NAME + ".ACTION_START_LISTENING";
    public static final String ACTION_STOP_LISTENING = PACKAGE_NAME + ".ACTION_STOP_LISTENING";
    public static final String LOCK_BLUETOOTH_SCAN_SERVICE = PACKAGE_NAME + ".OnBluetoothScanResultReceiver";
    public static final String LOCK_WIFI_SCAN_SERVICE = PACKAGE_NAME + ".OnWifiScanResultReceiver";
    public static final String LOCK_ALARM_SERVICE = PACKAGE_NAME + ".OnAlarmReceiver";
    public static final String LOCK_ACTIVITY_RECOGNITION_SERVICE = PACKAGE_NAME + ".OnActivityRecognitionResultReceiver";
    public static final String BLUETOOTH_LOG_FOLDER = "Bluetooth";
    public static final String WIFI_LOG_FOLDER = "Wifi";
    public static final String ACTIVITY_LOG_FOLDER = "Activity";
    public static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    public static final String AUDIO_RECORDER_FILE_EXT = ".mp3";
    public static final String LOG_FILE_EXT = ".txt";
    public static final String ROOT_FOLDER = "SmartRelationship";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST  = 9000;
    public static final long ACTIVITY_DETECTION_INTERVAL_IN_MILLISECONDS = 0;
    public static final String ACTION_ACTIVITY_RECOGNITION_RESULTS = PACKAGE_NAME + ".ACTION_ACTIVITY_RECOGNITION_RESULTS";

}