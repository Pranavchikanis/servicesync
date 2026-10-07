package com.servicesync.core.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PartConsumptionRequest {
    
    @NotNull(message = "Inventory ID is required")
    private Long inventoryId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public PartConsumptionRequest() {}

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
