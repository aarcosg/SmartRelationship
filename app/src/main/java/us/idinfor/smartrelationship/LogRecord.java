package us.idinfor.smartrelationship;


import android.bluetooth.BluetoothDevice;

import java.util.List;
import java.util.Set;

public class LogRecord {

    private Long id;
    private Type type;
    private Long time;
    private Set<BluetoothDevice> devices;
    private List<WifiNetwork> networks;

    public LogRecord(){}

    public LogRecord(Long id, Type type, Long time, Set<BluetoothDevice> devices, List<WifiNetwork> networks) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.devices = devices;
        this.networks = networks;
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

    public Set<BluetoothDevice> getDevices() {
        return devices;
    }

    public void setDevices(Set<BluetoothDevice> devices) {
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

    public enum Type{
        BLUETHOOTH, WIFI
    }
}
