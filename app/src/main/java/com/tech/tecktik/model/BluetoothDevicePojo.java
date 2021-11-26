package com.tech.tecktik.model;

public class BluetoothDevicePojo {

    public BluetoothDevicePojo(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    private String deviceName;
}
