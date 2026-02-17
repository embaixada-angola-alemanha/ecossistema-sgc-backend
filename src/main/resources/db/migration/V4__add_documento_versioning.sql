-- ============================================================
-- SGC — V4: Document versioning support + MinIO object key
-- ============================================================

ALTER TABLE documentos ADD COLUMN versao INTEGER NOT NULL DEFAULT 1;
ALTER TABLE documentos ADD COLUMN documento_original_id UUID REFERENCES documentos(id);
ALTER TABLE documentos ADD COLUMN ficheiro_object_key VARCHAR(500);

CREATE INDEX idx_documentos_original ON documentos(documento_original_id);
