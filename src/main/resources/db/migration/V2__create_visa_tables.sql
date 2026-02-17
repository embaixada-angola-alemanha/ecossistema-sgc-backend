-- V2: Visa Processing tables
CREATE TABLE visas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cidadao_id UUID NOT NULL REFERENCES cidadaos(id),
    tipo VARCHAR(50) NOT NULL,
    numero_visto VARCHAR(50) UNIQUE NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'RASCUNHO',
    nacionalidade_passaporte VARCHAR(100),
    motivo_viagem TEXT,
    data_entrada DATE,
    data_saida DATE,
    local_alojamento TEXT,
    entidade_convite VARCHAR(255),
    responsavel VARCHAR(100),
    valor_taxa DECIMAL(10,2) DEFAULT 0.00,
    taxa_paga BOOLEAN DEFAULT FALSE,
    data_submissao TIMESTAMP,
    data_decisao TIMESTAMP,
    motivo_rejeicao TEXT,
    observacoes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE TABLE visa_documentos (
    visa_id UUID NOT NULL REFERENCES visas(id) ON DELETE CASCADE,
    documento_id UUID NOT NULL REFERENCES documentos(id),
    PRIMARY KEY (visa_id, documento_id)
);

CREATE TABLE visa_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    visa_id UUID NOT NULL REFERENCES visas(id) ON DELETE CASCADE,
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

CREATE INDEX idx_visas_cidadao ON visas(cidadao_id);
CREATE INDEX idx_visas_estado ON visas(estado);
CREATE INDEX idx_visas_tipo ON visas(tipo);
CREATE INDEX idx_visas_numero ON visas(numero_visto);
CREATE INDEX idx_visa_historico_visa ON visa_historico(visa_id);
