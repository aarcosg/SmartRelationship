package us.idinfor.smartrelationship;


import java.util.List;
import java.util.Set;

import us.idinfor.smartrelationship.activityrecognition.DetectedActivity;
import us.idinfor.smartrelationship.bluetooth.BTDevice;
import us.idinfor.smartrelationship.wifi.WifiNetwork;

public class LogRecord {

    private Long id;
    private Type type;
    private Long time;
    private Set<BTDevice> devices;
    private List<WifiNetwork> networks;
    private List<DetectedActivity> activities;
    private Float azimuth;
    private Float pitch;
    private Float roll;

    public LogRecord(){}

    public LogRecord(Long id, Type type, Long time, Set<BTDevice> devices, List<WifiNetwork> networks, List<DetectedActivity> activities, Float azimuth, Float pitch, Float roll) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.devices = devices;
        this.networks = networks;
        this.activities = activities;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Set<BTDevice> getDevices() {
        return devices;
    }

    public void setDevices(Set<BTDevice> devices) {
        this.devices = devices;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<WifiNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<WifiNetwork> networks) {
        this.networks = networks;
    }

    public List<DetectedActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<DetectedActivity> activities) {
        this.activities = activities;
    }

    public Float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(Float azimuth) {
        this.azimuth = azimuth;
    }

    public Float getPitch() {
        return pitch;
    }

    public void setPitch(Float pitch) {
        this.pitch = pitch;
    }

    public Float getRoll() {
        return roll;
    }

    public void setRoll(Float roll) {
        this.roll = roll;
    }

    public enum Type{
        BLUETHOOTH, WIFI, ACTIVITY
    }
}
