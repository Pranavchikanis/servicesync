package com.servicesync.core.api.mapper;

import com.servicesync.core.api.dto.*;
import com.servicesync.core.domain.*;

public class DtoMapper {

    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }

    public static InventoryDTO toInventoryDTO(Inventory inventory) {
        if (inventory == null) return null;
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setSku(inventory.getSku());
        dto.setPartName(inventory.getPartName());
        dto.setQuantityInStock(inventory.getQuantityInStock());
        dto.setPrice(inventory.getPrice());
        return dto;
    }

    public static TicketPartDTO toTicketPartDTO(TicketPart part) {
        if (part == null) return null;
        TicketPartDTO dto = new TicketPartDTO();
        dto.setId(part.getId());
        if (part.getInventory() != null) {
            dto.setSku(part.getInventory().getSku());
            dto.setPartName(part.getInventory().getPartName());
        }
        dto.setQuantityUsed(part.getQuantityUsed());
        dto.setPriceAtTime(part.getPriceAtTime());
        return dto;
    }

    public static TicketDTO toTicketDTO(Ticket ticket) {
        if (ticket == null) return null;
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setCustomerName(ticket.getCustomerName());
        dto.setCustomerPhone(ticket.getCustomerPhone());
        dto.setDeviceInfo(ticket.getDeviceInfo());
        dto.setIssueDesc(ticket.getIssueDesc());
        dto.setStatus(ticket.getStatus().name());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setResolvedAt(ticket.getResolvedAt());
        dto.setTotalCost(ticket.getTotalCost());
        
        dto.setCreatedBy(toUserDTO(ticket.getCreatedBy()));
        dto.setTechnician(toUserDTO(ticket.getTechnician()));
        
        if (ticket.getParts() != null) {
            dto.setParts(ticket.getParts().stream().map(DtoMapper::toTicketPartDTO).toList());
        }
        return dto;
    }

    public static TicketHistoryDTO toTicketHistoryDTO(TicketHistory history) {
        if (history == null) return null;
        TicketHistoryDTO dto = new TicketHistoryDTO();
        dto.setId(history.getId());
        dto.setPreviousStatus(history.getPreviousStatus());
        dto.setNewStatus(history.getNewStatus());
        if (history.getChangedBy() != null) {
            dto.setChangedByName(history.getChangedBy().getName());
        }
        dto.setChangedAt(history.getTimestamp());
        dto.setNotes(history.getNotes());
        return dto;
    }
}
