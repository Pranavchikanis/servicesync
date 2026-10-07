package com.servicesync.core.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketCreateRequest {
    
    @NotBlank(message = "Customer name is required")
    @Size(max = 100)
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    @Size(max = 20)
    private String customerPhone;

    @NotBlank(message = "Device info is required")
    @Size(max = 255)
    private String deviceInfo;

    @NotBlank(message = "Issue description is required")
    private String issueDesc;

    public TicketCreateRequest() {}

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public String getIssueDesc() { return issueDesc; }
    public void setIssueDesc(String issueDesc) { this.issueDesc = issueDesc; }
}
