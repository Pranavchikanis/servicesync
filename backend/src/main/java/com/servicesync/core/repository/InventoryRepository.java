package com.servicesync.core.repository;

import com.servicesync.core.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySku(String sku);
    
    Page<Inventory> findByPartNameContainingIgnoreCase(String partName, Pageable pageable);
}
