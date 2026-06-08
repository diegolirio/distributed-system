CREATE TABLE transacao (
    id_transacao    BIGINT        NOT NULL AUTO_INCREMENT,
    id_conta        INT           NOT NULL,
    id_produto      INT           NOT NULL,
    id_tipo         INT           NOT NULL,
    id_contratacao  INT           NULL,
    valor           DECIMAL(15,2) NOT NULL,
    data_hora       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_idempotencia CHAR(36)      NOT NULL,
    CONSTRAINT pk_transacao      PRIMARY KEY (id_transacao),
    CONSTRAINT uq_trans_idem     UNIQUE (id_idempotencia),
    CONSTRAINT fk_trans_conta    FOREIGN KEY (id_conta)
        REFERENCES conta (id_conta)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_trans_produto  FOREIGN KEY (id_produto)
        REFERENCES produto (id_produto)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_trans_tipo     FOREIGN KEY (id_tipo)
        REFERENCES tipo_transacao (id_tipo)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_trans_contr    FOREIGN KEY (id_contratacao)
        REFERENCES contratacao (id_contratacao)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_trans_valor    CHECK (valor > 0)
) ENGINE = InnoDB;

CREATE INDEX ix_trans_conta    ON transacao (id_conta);
CREATE INDEX ix_trans_produto  ON transacao (id_produto);
CREATE INDEX ix_trans_data     ON transacao (data_hora);
