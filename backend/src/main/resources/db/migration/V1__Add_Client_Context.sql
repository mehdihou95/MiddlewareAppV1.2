-- This migration adds client context support
-- It's a placeholder to ensure proper versioning between V0 and V2

-- Add any missing indexes for client context
CREATE INDEX IF NOT EXISTS idx_interfaces_client_id ON interfaces(client_id); 