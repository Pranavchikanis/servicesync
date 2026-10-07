<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="refresh" content="30">
    <title>ServiceSync Kiosk Display</title>
    <!-- Use basic styling for the legacy kiosk, simulating an old, robust interface -->
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: auto;
            background: white;
            padding: 20px;
            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
        }
        h1 {
            text-align: center;
            color: #333;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            font-size: 1.2rem;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #007bff;
            color: white;
        }
        tr:nth-child(even) {
            background-color: #f2f2f2;
        }
        .status-CREATED { color: #856404; font-weight: bold; }
        .status-DIAGNOSING { color: #17a2b8; font-weight: bold; }
        .status-WAITING_PARTS { color: #dc3545; font-weight: bold; }
        .status-RESOLVED { color: #28a745; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Active Repairs</h1>
        <p style="text-align: center; color: #555;">(Auto-refreshes every 30 seconds)</p>
        
        <table>
            <thead>
                <tr>
                    <th>Ticket ID</th>
                    <th>Device Information</th>
                    <th>Current Status</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty activeTickets}">
                        <c:forEach var="ticket" items="${activeTickets}">
                            <tr>
                                <td><c:out value="${ticket.id}" /></td>
                                <td><c:out value="${ticket.deviceInfo}" /></td>
                                <td class="status-${ticket.status}"><c:out value="${ticket.status}" /></td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="3" style="text-align: center;">No active tickets in the display queue.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</body>
</html>
