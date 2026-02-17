-- ============================================================
-- SGC — Sistema de Gestao Consular
-- V1: Foundational schema
-- ============================================================

-- 1. cidadaos — Citizen registry
CREATE TABLE cidadaos (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_passaporte VARCHAR(50) NOT NULL UNIQUE,
    nome_completo   VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    sexo            VARCHAR(20),
    nacionalidade   VARCHAR(100) DEFAULT 'Angolana',
    estado_civil    VARCHAR(30),
    email           VARCHAR(255),
    telefone        VARCHAR(50),
    endereco_angola TEXT,
    endereco_alemanha TEXT,
    estado          VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cidadaos_estado ON cidadaos(estado);
CREATE INDEX idx_cidadaos_nome   ON cidadaos(nome_completo);

-- 2. documentos — Document tracking
CREATE TABLE documentos (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cidadao_id      UUID NOT NULL REFERENCES cidadaos(id),
    tipo            VARCHAR(50) NOT NULL,
    numero          VARCHAR(100),
    data_emissao    DATE,
    data_validade   DATE,
    ficheiro_url    VARCHAR(500),
    ficheiro_nome   VARCHAR(255),
    ficheiro_tamanho BIGINT,
    ficheiro_tipo   VARCHAR(100),
    estado          VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_documentos_cidadao ON documentos(cidadao_id);
CREATE INDEX idx_documentos_tipo    ON documentos(tipo);
CREATE INDEX idx_documentos_estado  ON documentos(estado);

-- 3. processos — Consular cases
CREATE TABLE processos (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cidadao_id      UUID NOT NULL REFERENCES cidadaos(id),
    tipo            VARCHAR(50) NOT NULL,
    numero_processo VARCHAR(50) NOT NULL UNIQUE,
    descricao       TEXT,
    estado          VARCHAR(30) NOT NULL DEFAULT 'RASCUNHO',
    prioridade      VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    responsavel     VARCHAR(255),
    valor_taxa      DECIMAL(10,2) DEFAULT 0,
    taxa_paga       BOOLEAN NOT NULL DEFAULT FALSE,
    data_submissao  TIMESTAMP,
    data_conclusao  TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_processos_cidadao  ON processos(cidadao_id);
CREATE INDEX idx_processos_tipo     ON processos(tipo);
CREATE INDEX idx_processos_estado   ON processos(estado);

-- 4. processo_documentos — Junction table process <-> document
CREATE TABLE processo_documentos (
    processo_id     UUID NOT NULL REFERENCES processos(id),
    documento_id    UUID NOT NULL REFERENCES documentos(id),
    PRIMARY KEY (processo_id, documento_id)
);

CREATE INDEX idx_proc_doc_processo  ON processo_documentos(processo_id);
CREATE INDEX idx_proc_doc_documento ON processo_documentos(documento_id);

-- 5. processo_historico — State change history
CREATE TABLE processo_historico (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    processo_id     UUID NOT NULL REFERENCES processos(id),
    estado_anterior VARCHAR(30),
    estado_novo     VARCHAR(30) NOT NULL,
    comentario      TEXT,
    alterado_por    VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_proc_hist_processo ON processo_historico(processo_id);

-- 6. audit_log — Audit trail
CREATE TABLE audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utilizador_id   VARCHAR(255),
    accao           VARCHAR(50) NOT NULL,
    recurso_tipo    VARCHAR(100) NOT NULL,
    recurso_id      VARCHAR(255),
    detalhes        TEXT,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_audit_utilizador ON audit_log(utilizador_id);
CREATE INDEX idx_audit_recurso    ON audit_log(recurso_tipo, recurso_id);
