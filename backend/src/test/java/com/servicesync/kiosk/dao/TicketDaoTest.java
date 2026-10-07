package com.servicesync.kiosk.dao;

import com.servicesync.kiosk.model.DisplayTicket;
import com.servicesync.kiosk.util.DatabaseUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TicketDaoTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseUtil> mockedDatabaseUtil;

    @BeforeEach
    public void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseUtil = mockStatic(DatabaseUtil.class);
        mockedDatabaseUtil.when(DatabaseUtil::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @AfterEach
    public void tearDown() {
        mockedDatabaseUtil.close();
    }

    @Test
    public void testGetActiveTickets() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getString("device_info")).thenReturn("Laptop", "Phone");
        when(mockResultSet.getString("status")).thenReturn("DIAGNOSING", "IN_REPAIR");

        TicketDao ticketDao = new TicketDao();
        List<DisplayTicket> tickets = ticketDao.getActiveTickets();

        assertEquals(2, tickets.size());

        assertEquals(1L, tickets.get(0).getId());
        assertEquals("Laptop", tickets.get(0).getDeviceInfo());
        assertEquals("DIAGNOSING", tickets.get(0).getStatus());

        assertEquals(2L, tickets.get(1).getId());
        assertEquals("Phone", tickets.get(1).getDeviceInfo());
        assertEquals("IN_REPAIR", tickets.get(1).getStatus());

        verify(mockConnection, times(1)).prepareStatement("SELECT id, device_info, status FROM active_display_tickets");
        verify(mockPreparedStatement, times(1)).executeQuery();
    }
}
