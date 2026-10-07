package com.servicesync.core.api.controller;

import com.servicesync.core.api.dto.InventoryDTO;
import com.servicesync.core.api.mapper.DtoMapper;
import com.servicesync.core.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'TECH')")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Page<InventoryDTO> listInventory(@RequestParam(value = "name", required = false) String name, Pageable pageable) {
        return inventoryService.getPaginatedInventory(pageable, name)
                .map(DtoMapper::toInventoryDTO);
    }
}
