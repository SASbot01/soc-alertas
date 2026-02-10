-- Make company_id nullable for superadmin users
ALTER TABLE users DROP FOREIGN KEY users_ibfk_1;
ALTER TABLE users MODIFY COLUMN company_id VARCHAR(255) NULL;
ALTER TABLE users ADD CONSTRAINT fk_users_company_id
    FOREIGN KEY (company_id) REFERENCES companies(id);

-- Index for user lookups by company
CREATE INDEX idx_users_company ON users(company_id);

-- Seed superadmin (password: superadmin)
INSERT INTO users (id, email, full_name, password, role, company_id, is_active, created_at)
VALUES (
    'superadmin-001',
    'superadmin@blackwolf.io',
    'Super Admin',
    '$2b$10$kr6D76MZQjeOn5Ae.Gy2TuvOSopDobY4LlguphJ8YBOn8klxd9UzK',
    'superadmin',
    NULL,
    TRUE,
    NOW()
);
