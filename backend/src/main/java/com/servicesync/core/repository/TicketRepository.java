package com.servicesync.core.repository;

import com.servicesync.core.domain.Ticket;
import com.servicesync.core.domain.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    
    Optional<Ticket> findByIdAndCustomerPhone(Long id, String customerPhone);
    
    @Query("SELECT t FROM Ticket t WHERE t.status IN :statuses AND t.createdAt < :threshold")
    List<Ticket> findTicketsExceedingSla(@Param("statuses") List<TicketStatus> statuses, @Param("threshold") LocalDateTime threshold);
    
    @Query("SELECT t.id FROM Ticket t WHERE t.status IN :statuses AND t.createdAt < :threshold")
    List<Long> findTicketIdsExceedingSla(@Param("statuses") List<TicketStatus> statuses, @Param("threshold") LocalDateTime threshold);
    
    @EntityGraph(attributePaths = {"createdBy", "technician"})
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"parts", "parts.inventory"})
    Optional<Ticket> findWithPartsById(Long id);
}
