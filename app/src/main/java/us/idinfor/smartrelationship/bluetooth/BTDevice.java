package us.idinfor.smartrelationship.bluetooth;


public class BTDevice {

    private String name;
    private String address;
    private String majorClass;

    public BTDevice(String name, String address, String majorClass) {
        this.name = name;
        this.address = address;
        this.majorClass = majorClass;
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
