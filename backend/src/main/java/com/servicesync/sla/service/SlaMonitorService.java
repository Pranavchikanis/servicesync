package com.servicesync.sla.service;

import com.servicesync.sla.domain.NotificationLog;
import com.servicesync.sla.repository.NotificationLogRepository;
import com.servicesync.sla.client.CoreTicketClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaMonitorService {

    private final NotificationLogRepository notificationLogRepository;
    private final CoreTicketClient coreTicketClient;
    private static final String EVENT_SLA_BREACH = "SLA_BREACH";

    public SlaMonitorService(NotificationLogRepository notificationLogRepository, CoreTicketClient coreTicketClient) {
        this.notificationLogRepository = notificationLogRepository;
        this.coreTicketClient = coreTicketClient;
    }

    // Run every minute for MVP testing purposes. In production, this might be hourly.
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkSlaBreaches() {
        // Tickets in CREATED or DIAGNOSING older than 48 hours
        LocalDateTime threshold = LocalDateTime.now().minusHours(48);
        
        List<Long> breachedTicketIds = coreTicketClient.getSlaBreachedTicketIds(threshold);
        
        for (Long ticketId : breachedTicketIds) {
            // Only insert SLA breach if one doesn't already exist for this ticket
            if (!notificationLogRepository.existsByTicketIdAndEventType(ticketId, EVENT_SLA_BREACH)) {
                NotificationLog log = new NotificationLog(ticketId, EVENT_SLA_BREACH, "SENT");
                notificationLogRepository.save(log);
                // System.out.println("Generated SLA Breach notification for Ticket " + ticketId);
            }
        }
    }
}
