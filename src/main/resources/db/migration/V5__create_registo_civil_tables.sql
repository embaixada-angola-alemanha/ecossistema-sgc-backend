-- V5: Civil Registry tables
CREATE TABLE registos_civis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cidadao_id UUID NOT NULL REFERENCES cidadaos(id),
    tipo VARCHAR(50) NOT NULL,
    numero_registo VARCHAR(50) UNIQUE NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'RASCUNHO',
    data_evento DATE,
    local_evento VARCHAR(255),
    observacoes TEXT,
    responsavel VARCHAR(100),
    motivo_rejeicao TEXT,
    data_submissao TIMESTAMP,
    data_verificacao TIMESTAMP,
    data_certificado TIMESTAMP,
    -- Birth-specific
    nome_pai VARCHAR(255),
    nome_mae VARCHAR(255),
    local_nascimento VARCHAR(255),
    -- Marriage-specific
    nome_conjuge1 VARCHAR(255),
    nome_conjuge2 VARCHAR(255),
    regime_casamento VARCHAR(100),
    -- Death-specific
    causa_obito TEXT,
    local_obito VARCHAR(255),
    data_obito DATE,
    -- Certificate storage
    certificado_object_key VARCHAR(500),
    certificado_url VARCHAR(500),
    -- Base entity
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE TABLE registo_civil_documentos (
    registo_civil_id UUID NOT NULL REFERENCES registos_civis(id) ON DELETE CASCADE,
    documento_id UUID NOT NULL REFERENCES documentos(id),
    PRIMARY KEY (registo_civil_id, documento_id)
);

CREATE TABLE registo_civil_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    registo_civil_id UUID NOT NULL REFERENCES registos_civis(id) ON DELETE CASCADE,
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

CREATE INDEX idx_registos_civis_cidadao ON registos_civis(cidadao_id);
CREATE INDEX idx_registos_civis_estado ON registos_civis(estado);
CREATE INDEX idx_registos_civis_tipo ON registos_civis(tipo);
CREATE INDEX idx_registos_civis_numero ON registos_civis(numero_registo);
CREATE INDEX idx_rc_historico_registo ON registo_civil_historico(registo_civil_id);
