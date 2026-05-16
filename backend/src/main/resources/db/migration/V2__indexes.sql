CREATE INDEX idx_customers_active_name ON customers (active, name);
CREATE INDEX idx_customers_phone ON customers (phone);

CREATE INDEX idx_professionals_active_name ON professionals (active, name);
CREATE INDEX idx_professionals_phone ON professionals (phone);

CREATE INDEX idx_services_active_name ON services (active, name);
