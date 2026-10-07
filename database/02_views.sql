CREATE VIEW active_display_tickets AS 
SELECT id, device_info, status 
FROM tickets 
WHERE status != 'CLOSED';
