-- Create default client
INSERT INTO clients (name, code, description, status, created_at, updated_at)
VALUES ('DEFAULT_CLIENT', 'DEFAULT', 'Default client for existing data', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Update existing records with default client
UPDATE asn_headers SET client_id = (SELECT MAX(id) FROM clients) WHERE client_id IS NULL;
UPDATE asn_lines SET client_id = (SELECT MAX(id) FROM clients) WHERE client_id IS NULL;
UPDATE processed_files SET client_id = (SELECT MAX(id) FROM clients) WHERE client_id IS NULL;
UPDATE mapping_rules SET client_id = (SELECT MAX(id) FROM clients) WHERE client_id IS NULL;

-- Make client_id columns NOT NULL after migration
ALTER TABLE asn_headers ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE asn_lines ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE processed_files ALTER COLUMN client_id SET NOT NULL;
ALTER TABLE mapping_rules ALTER COLUMN client_id SET NOT NULL; 