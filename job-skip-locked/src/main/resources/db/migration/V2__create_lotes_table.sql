CREATE TABLE lotes (
    id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data_lote  DATE           NOT NULL,
    created_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_lotes_data_lote UNIQUE (data_lote)
);
