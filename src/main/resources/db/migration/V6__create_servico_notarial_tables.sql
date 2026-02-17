-- V6: Notarial Services tables
CREATE TABLE servicos_notariais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cidadao_id UUID NOT NULL REFERENCES cidadaos(id),
    tipo VARCHAR(50) NOT NULL,
    numero_servico VARCHAR(50) UNIQUE NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'RASCUNHO',
    descricao TEXT,
    observacoes TEXT,
    responsavel VARCHAR(100),
    motivo_rejeicao TEXT,
    valor_taxa NUMERIC(10,2) NOT NULL DEFAULT 0,
    taxa_paga BOOLEAN NOT NULL DEFAULT false,
    data_submissao TIMESTAMP,
    data_conclusao TIMESTAMP,
    -- Power of attorney (PROCURACAO)
    outorgante VARCHAR(255),
    outorgado VARCHAR(255),
    finalidade_procuracao TEXT,
    -- Legalization (LEGALIZACAO)
    documento_origem VARCHAR(255),
    pais_origem VARCHAR(100),
    entidade_emissora VARCHAR(255),
    -- Apostille (APOSTILA)
    documento_apostilado VARCHAR(255),
    pais_destino VARCHAR(100),
    -- Certified copy (COPIA_CERTIFICADA)
    documento_original_ref VARCHAR(255),
    numero_copias INTEGER DEFAULT 1,
    -- Certificate storage
    certificado_object_key VARCHAR(500),
    certificado_url VARCHAR(500),
    -- Appointment link
    agendamento_id UUID REFERENCES agendamentos(id),
    -- Base entity
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE TABLE servico_notarial_documentos (
    servico_notarial_id UUID NOT NULL REFERENCES servicos_notariais(id) ON DELETE CASCADE,
    documento_id UUID NOT NULL REFERENCES documentos(id),
    PRIMARY KEY (servico_notarial_id, documento_id)
);

CREATE TABLE servico_notarial_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    servico_notarial_id UUID NOT NULL REFERENCES servicos_notariais(id) ON DELETE CASCADE,
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

CREATE INDEX idx_servicos_notariais_cidadao ON servicos_notariais(cidadao_id);
CREATE INDEX idx_servicos_notariais_estado ON servicos_notariais(estado);
CREATE INDEX idx_servicos_notariais_tipo ON servicos_notariais(tipo);
CREATE INDEX idx_servicos_notariais_numero ON servicos_notariais(numero_servico);
CREATE INDEX idx_sn_historico_servico ON servico_notarial_historico(servico_notarial_id);
