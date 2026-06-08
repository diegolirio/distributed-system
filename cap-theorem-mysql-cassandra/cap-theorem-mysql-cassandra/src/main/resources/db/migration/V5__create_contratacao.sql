CREATE TABLE contratacao (
    id_contratacao   INT  NOT NULL AUTO_INCREMENT,
    id_cliente       INT  NOT NULL,
    id_produto       INT  NOT NULL,
    data_contratacao DATE NOT NULL,
    status           ENUM('ATIVA','SUSPENSA','LIQUIDADA','CANCELADA') NOT NULL DEFAULT 'ATIVA',
    CONSTRAINT pk_contratacao        PRIMARY KEY (id_contratacao),
    CONSTRAINT uq_contr_unica        UNIQUE (id_cliente, id_produto, data_contratacao),
    CONSTRAINT fk_contr_cliente      FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_contr_produto      FOREIGN KEY (id_produto)
        REFERENCES produto (id_produto)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX ix_contr_cliente ON contratacao (id_cliente);
CREATE INDEX ix_contr_produto ON contratacao (id_produto);
