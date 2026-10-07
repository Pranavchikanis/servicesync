package com.servicesync.kiosk.dao;

import com.servicesync.kiosk.model.DisplayTicket;
import com.servicesync.kiosk.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketDao {

    public List<DisplayTicket> getActiveTickets() {
        List<DisplayTicket> tickets = new ArrayList<>();
        String sql = "SELECT id, device_info, status FROM active_display_tickets";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                DisplayTicket ticket = new DisplayTicket();
                ticket.setId(resultSet.getLong("id"));
                ticket.setDeviceInfo(resultSet.getString("device_info"));
                ticket.setStatus(resultSet.getString("status"));
                tickets.add(ticket);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            // In a real legacy system, this might log to System.err or log4j
        }

        return tickets;
    }
}
