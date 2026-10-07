package com.servicesync.core.event;

import com.servicesync.core.domain.Ticket;

public class TicketStatusChangedEvent {
    private final Ticket ticket;
    private final String previousStatus;
    private final String newStatus;
    private final Long changedById;
    private final String notes;

    public TicketStatusChangedEvent(Ticket ticket, String previousStatus, String newStatus, Long changedById, String notes) {
        this.ticket = ticket;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedById = changedById;
        this.notes = notes;
    }

    public Ticket getTicket() { return ticket; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public Long getChangedById() { return changedById; }
    public String getNotes() { return notes; }
}
