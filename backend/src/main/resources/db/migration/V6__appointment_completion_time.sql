ALTER TABLE appointments
    ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_appointments_completed_at ON appointments (completed_at);
