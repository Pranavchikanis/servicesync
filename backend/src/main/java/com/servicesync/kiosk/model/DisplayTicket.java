package com.servicesync.kiosk.model;

public class DisplayTicket {
    private Long id;
    private String deviceInfo;
    private String status;

    public DisplayTicket() {
    }

    public DisplayTicket(Long id, String deviceInfo, String status) {
        this.id = id;
        this.deviceInfo = deviceInfo;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
