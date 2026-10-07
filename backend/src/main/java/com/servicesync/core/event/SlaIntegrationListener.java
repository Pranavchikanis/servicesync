package com.servicesync.core.event;

import com.servicesync.core.api.dto.SlaEventRequest;
import com.servicesync.core.service.SlaFeignClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SlaIntegrationListener {

    private static final Logger logger = LoggerFactory.getLogger(SlaIntegrationListener.class);
    private final SlaFeignClient slaFeignClient;

    public SlaIntegrationListener(SlaFeignClient slaFeignClient) {
        this.slaFeignClient = slaFeignClient;
    }

    // Fires after the core transaction commits, preventing the core from rolling back if this fails
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async // Or run synchronously, but we catch exceptions
    public void handleTicketStatusChangedEvent(TicketStatusChangedEvent event) {
        try {
            SlaEventRequest request = new SlaEventRequest(
                    event.getTicket().getId(),
                    "STATE_CHANGE",
                    event.getNewStatus()
            );
            slaFeignClient.sendSlaEvent(request);
            logger.debug("Successfully notified SLA microservice for Ticket {}", event.getTicket().getId());
        } catch (FeignException e) {
            logger.warn("Failed to notify SLA microservice for Ticket {}: {}", event.getTicket().getId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error notifying SLA microservice", e);
        }
    }
}
