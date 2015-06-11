package us.idinfor.smartrelationship.wifi;


public class WifiNetwork {

    //The address of the access point.
    private String BSSID;
    //The network name.
    private String SSID;
    //The frequency in MHz of the channel over which the client is communicating with the access point.
    private int frequency;
    //The detected signal level in dBm, also known as the RSSI.
    private int level;
    //timestamp in microseconds (since boot) when this result was last seen.
    private long timestamp;

    public WifiNetwork(){}

    public WifiNetwork(String BSSID, String SSID, int frequency, int level, long timestamp) {
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.frequency = frequency;
        this.level = level;
        this.timestamp = timestamp;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
