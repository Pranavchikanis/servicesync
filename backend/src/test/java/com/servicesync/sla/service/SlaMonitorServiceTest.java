package com.servicesync.sla.service;

import com.servicesync.sla.client.CoreTicketClient;
import com.servicesync.sla.domain.NotificationLog;
import com.servicesync.sla.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlaMonitorServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private CoreTicketClient coreTicketClient;

    @InjectMocks
    private SlaMonitorService slaMonitorService;

    @Test
    void testCheckSlaBreaches_WithBreachedTickets_GeneratesLogs() {
        when(coreTicketClient.getSlaBreachedTicketIds(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(1L, 2L));

        when(notificationLogRepository.existsByTicketIdAndEventType(1L, "SLA_BREACH")).thenReturn(false);
        when(notificationLogRepository.existsByTicketIdAndEventType(2L, "SLA_BREACH")).thenReturn(true);

        slaMonitorService.checkSlaBreaches();

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(logCaptor.capture());

        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getTicketId()).isEqualTo(1L);
        assertThat(savedLog.getEventType()).isEqualTo("SLA_BREACH");
    }

    @Test
    void testCheckSlaBreaches_NoBreachedTickets_DoesNothing() {
        when(coreTicketClient.getSlaBreachedTicketIds(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        slaMonitorService.checkSlaBreaches();

        verify(notificationLogRepository, never()).existsByTicketIdAndEventType(anyLong(), anyString());
        verify(notificationLogRepository, never()).save(any(NotificationLog.class));
    }
}
