-- V9: Add keycloak_id column to cidadaos for linking citizen accounts
ALTER TABLE cidadaos ADD COLUMN keycloak_id VARCHAR(255);
CREATE UNIQUE INDEX idx_cidadaos_keycloak_id ON cidadaos(keycloak_id) WHERE keycloak_id IS NOT NULL;
