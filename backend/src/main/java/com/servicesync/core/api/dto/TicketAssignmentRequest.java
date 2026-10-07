package com.servicesync.core.api.dto;

import jakarta.validation.constraints.NotNull;

public class TicketAssignmentRequest {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    public TicketAssignmentRequest() {}

    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
}
