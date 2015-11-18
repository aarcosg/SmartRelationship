package us.idinfor.smartrelationship.bluetooth;


public class BTDevice {

    private String name;
    private String address;
    private String majorClass;
    private Short rssi;

    public BTDevice(String name, String address, String majorClass, Short rssi) {
        this.name = name;
        this.address = address;
        this.majorClass = majorClass;
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMajorClass() {
        return majorClass;
    }

    public void setMajorClass(String majorClass) {
        this.majorClass = majorClass;
    }

    public Short getRssi() {
        return rssi;
    }

    public void setRssi(Short rssi) {
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BTDevice btDevice = (BTDevice) o;

        return getAddress().equals(btDevice.getAddress());

    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }
}
