package com.servicesync.core.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDTO {
    private Long id;
    private String customerName;
    private String customerPhone;
    private String deviceInfo;
    private String issueDesc;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private BigDecimal totalCost;
    
    private UserDTO createdBy;
    private UserDTO technician;
    
    private List<TicketPartDTO> parts;

    public TicketDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public String getIssueDesc() { return issueDesc; }
    public void setIssueDesc(String issueDesc) { this.issueDesc = issueDesc; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public UserDTO getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserDTO createdBy) { this.createdBy = createdBy; }

    public UserDTO getTechnician() { return technician; }
    public void setTechnician(UserDTO technician) { this.technician = technician; }

    public List<TicketPartDTO> getParts() { return parts; }
    public void setParts(List<TicketPartDTO> parts) { this.parts = parts; }
}
