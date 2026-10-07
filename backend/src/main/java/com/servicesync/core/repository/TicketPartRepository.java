package com.servicesync.core.repository;

import com.servicesync.core.domain.TicketPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPartRepository extends JpaRepository<TicketPart, Long> {
    List<TicketPart> findByTicketId(Long ticketId);
}
