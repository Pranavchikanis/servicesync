package com.servicesync.sla.service;

import com.servicesync.sla.domain.NotificationLog;
import com.servicesync.sla.dto.SlaEventRequestDto;
import com.servicesync.sla.repository.NotificationLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlaProcessorService {

    private final NotificationLogRepository notificationLogRepository;

    public SlaProcessorService(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Async
    @Transactional
    public void processEvent(SlaEventRequestDto request) {
        // Prevent duplicate processing of the exact same event type for a ticket (e.g., duplicate RESOLVED events)
        if (notificationLogRepository.existsByTicketIdAndEventType(request.getTicketId(), request.getEventType())) {
            return;
        }

        NotificationLog log = new NotificationLog(request.getTicketId(), request.getEventType(), "SENT");
        notificationLogRepository.save(log);
        
        // In a real system, we would trigger an email/SMS dispatch here.
        // System.out.println("Dispatched notification for Ticket " + request.getTicketId() + ": " + request.getEventType());
    }
}
