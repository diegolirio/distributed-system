CREATE TABLE conta (
    id_conta   INT            NOT NULL AUTO_INCREMENT,
    id_cliente INT            NOT NULL,
    numero     VARCHAR(20)    NOT NULL,
    tipo_conta ENUM('PF','PJ') NOT NULL,
    saldo      DECIMAL(15,2)  NOT NULL DEFAULT 0.00,
    status     ENUM('ATIVA','BLOQUEADA','ENCERRADA') NOT NULL DEFAULT 'ATIVA',
    CONSTRAINT pk_conta         PRIMARY KEY (id_conta),
    CONSTRAINT uq_conta_numero  UNIQUE (numero),
    CONSTRAINT fk_conta_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_conta_saldo   CHECK (saldo >= -1000000.00)
) ENGINE = InnoDB;

CREATE INDEX ix_conta_cliente ON conta (id_cliente);
