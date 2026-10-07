package com.servicesync.sla.repository;

import com.servicesync.sla.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {


    
    @Query(value = "SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NotificationLog n WHERE n.ticketId = :ticketId AND n.eventType = :eventType")
    boolean existsByTicketIdAndEventType(@Param("ticketId") Long ticketId, @Param("eventType") String eventType);
}
