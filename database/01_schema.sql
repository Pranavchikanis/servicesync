CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TECH') NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_name VARCHAR(150) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    price DECIMAL(10,2) NOT NULL,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_inventory_sku UNIQUE (sku),
    CONSTRAINT chk_inventory_qty CHECK (quantity_in_stock >= 0)
);

CREATE TABLE tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    device_info VARCHAR(255) NOT NULL,
    issue_desc TEXT NOT NULL,
    status ENUM('CREATED', 'DIAGNOSING', 'WAITING_PARTS', 'IN_REPAIR', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'CREATED',
    created_by_id BIGINT NOT NULL,
    technician_id BIGINT NULL,
    total_cost DECIMAL(10,2) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_tickets_created_by FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_technician FOREIGN KEY (technician_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_lookup ON tickets(id, customer_phone);
CREATE INDEX idx_tickets_tech ON tickets(technician_id);

CREATE TABLE ticket_parts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    quantity_used INT NOT NULL DEFAULT 1,
    price_at_time DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_tp_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_tp_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE RESTRICT,
    CONSTRAINT chk_tp_qty CHECK (quantity_used > 0)
);

CREATE TABLE ticket_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    previous_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    notes TEXT NULL,
    changed_by_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_th_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_th_changed_by FOREIGN KEY (changed_by_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
