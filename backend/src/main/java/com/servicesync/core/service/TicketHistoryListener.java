package com.servicesync.core.service;

import com.servicesync.core.domain.TicketHistory;
import com.servicesync.core.domain.User;
import com.servicesync.core.event.TicketStatusChangedEvent;
import com.servicesync.core.repository.TicketHistoryRepository;
import com.servicesync.core.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketHistoryListener {

    private final TicketHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public TicketHistoryListener(TicketHistoryRepository historyRepository, UserRepository userRepository) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void onTicketStatusChanged(TicketStatusChangedEvent event) {
        User user = userRepository.findById(event.getChangedById())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TicketHistory history = new TicketHistory();
        history.setTicket(event.getTicket());
        history.setPreviousStatus(event.getPreviousStatus());
        history.setNewStatus(event.getNewStatus());
        history.setNotes(event.getNotes());
        history.setChangedBy(user);

        historyRepository.save(history);
    }
}
