CREATE TABLE tipo_transacao (
    id_tipo    INT         NOT NULL AUTO_INCREMENT,
    descricao  VARCHAR(60) NOT NULL,
    sinal      INT         NOT NULL,
    CONSTRAINT pk_tipo       PRIMARY KEY (id_tipo),
    CONSTRAINT uq_tipo_desc  UNIQUE (descricao),
    CONSTRAINT ck_tipo_sinal CHECK (sinal IN (-1, 1))
) ENGINE = InnoDB;
