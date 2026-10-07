package com.servicesync.sla.dto;

public class SlaEventRequestDto {
    private Long ticketId;
    private String eventType;
    private String newStatus;

    public SlaEventRequestDto() {
    }

    public SlaEventRequestDto(Long ticketId, String eventType, String newStatus) {
        this.ticketId = ticketId;
        this.eventType = eventType;
        this.newStatus = newStatus;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}
