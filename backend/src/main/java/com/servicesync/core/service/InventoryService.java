package com.servicesync.core.service;

import com.servicesync.core.domain.Inventory;
import com.servicesync.core.exception.ResourceNotFoundException;
import com.servicesync.core.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Inventory addStock(Long inventoryId, int quantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
                
        inventory.setQuantityInStock(inventory.getQuantityInStock() + quantity);
        return inventoryRepository.save(inventory);
    }
    
    public Inventory updatePrice(Long inventoryId, BigDecimal newPrice) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
                
        inventory.setPrice(newPrice);
        return inventoryRepository.save(inventory);
    }
    
    @Transactional(readOnly = true)
    public Page<Inventory> getPaginatedInventory(Pageable pageable, String name) {
        if (name != null && !name.trim().isEmpty()) {
            return inventoryRepository.findByPartNameContainingIgnoreCase(name.trim(), pageable);
        }
        return inventoryRepository.findAll(pageable);
    }
}
