-- V7: Notification preferences and log tables
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cidadao_id UUID NOT NULL REFERENCES cidadaos(id) ON DELETE CASCADE,
    workflow_name VARCHAR(50) NOT NULL,
    email_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_notification_pref_cidadao_workflow UNIQUE (cidadao_id, workflow_name)
);

CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cidadao_id UUID REFERENCES cidadaos(id) ON DELETE SET NULL,
    to_address VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template VARCHAR(100) NOT NULL,
    workflow_name VARCHAR(50) NOT NULL,
    entity_id UUID,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    error_message TEXT,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_notification_pref_cidadao ON notification_preferences(cidadao_id);
CREATE INDEX idx_notification_logs_cidadao ON notification_logs(cidadao_id);
CREATE INDEX idx_notification_logs_estado ON notification_logs(estado);
CREATE INDEX idx_notification_logs_workflow ON notification_logs(workflow_name);
CREATE INDEX idx_notification_logs_entity ON notification_logs(entity_id);
