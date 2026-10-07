package com.servicesync.sla.service;

import com.servicesync.sla.domain.NotificationLog;
import com.servicesync.sla.dto.SlaEventRequestDto;
import com.servicesync.sla.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlaProcessorServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @InjectMocks
    private SlaProcessorService slaProcessorService;

    private SlaEventRequestDto request;

    @BeforeEach
    void setUp() {
        request = new SlaEventRequestDto();
        request.setTicketId(10L);
        request.setEventType("DIAGNOSING");
    }

    @Test
    void testProcessEvent_Success() {
        when(notificationLogRepository.existsByTicketIdAndEventType(10L, "DIAGNOSING")).thenReturn(false);

        slaProcessorService.processEvent(request);

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(logCaptor.capture());

        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getTicketId()).isEqualTo(10L);
        assertThat(savedLog.getEventType()).isEqualTo("DIAGNOSING");
        assertThat(savedLog.getStatus()).isEqualTo("SENT");
    }

    @Test
    void testProcessEvent_DuplicateEvent_Ignored() {
        when(notificationLogRepository.existsByTicketIdAndEventType(10L, "DIAGNOSING")).thenReturn(true);

        slaProcessorService.processEvent(request);

        verify(notificationLogRepository, never()).save(any(NotificationLog.class));
    }
}
