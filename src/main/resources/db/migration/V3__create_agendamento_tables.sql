-- V3: Appointment/Booking system tables
CREATE TABLE agendamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cidadao_id UUID NOT NULL REFERENCES cidadaos(id),
    tipo VARCHAR(50) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    numero_agendamento VARCHAR(50) UNIQUE NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    duracao_minutos INTEGER NOT NULL DEFAULT 30,
    local VARCHAR(255) DEFAULT 'Embaixada de Angola — Berlim',
    notas TEXT,
    motivo_cancelamento TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE TABLE agendamento_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agendamento_id UUID NOT NULL REFERENCES agendamentos(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(50),
    estado_novo VARCHAR(50) NOT NULL,
    comentario TEXT,
    alterado_por VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_agendamentos_cidadao ON agendamentos(cidadao_id);
CREATE INDEX idx_agendamentos_estado ON agendamentos(estado);
CREATE INDEX idx_agendamentos_tipo ON agendamentos(tipo);
CREATE INDEX idx_agendamentos_data_hora ON agendamentos(data_hora);
CREATE INDEX idx_agendamentos_numero ON agendamentos(numero_agendamento);
CREATE INDEX idx_agendamento_historico_agendamento ON agendamento_historico(agendamento_id);
