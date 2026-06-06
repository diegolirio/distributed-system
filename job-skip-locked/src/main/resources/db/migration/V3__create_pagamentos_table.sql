CREATE TABLE pagamentos (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    proposta_id VARCHAR2(255)  NOT NULL,
    valor       NUMBER(19, 2)  NOT NULL,
    lote_id     NUMBER         NOT NULL,
    request_id  NUMBER         NOT NULL,
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pagamentos_lotes FOREIGN KEY (lote_id) REFERENCES lotes (id)
    -- request_id is intentionally NOT a FK to requests to avoid lock contention with SKIP LOCKED
);
