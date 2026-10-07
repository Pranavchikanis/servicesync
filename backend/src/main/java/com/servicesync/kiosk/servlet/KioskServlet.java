package com.servicesync.kiosk.servlet;

import com.servicesync.kiosk.dao.TicketDao;
import com.servicesync.kiosk.model.DisplayTicket;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class KioskServlet extends HttpServlet {

    private TicketDao ticketDao;

    @Override
    public void init() throws ServletException {
        ticketDao = new TicketDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<DisplayTicket> activeTickets = ticketDao.getActiveTickets();
        request.setAttribute("activeTickets", activeTickets);
        
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
