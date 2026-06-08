CREATE TABLE cliente (
    id_cliente INT          NOT NULL AUTO_INCREMENT,
    cpf        CHAR(11)     NOT NULL,
    cnpj       CHAR(14)     NULL,
    nome       VARCHAR(150) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    segmento   ENUM('MEDICO','DENTISTA','FISIOTERAPEUTA','OUTRO') NOT NULL,
    criado_em  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cliente       PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_cpf   UNIQUE (cpf),
    CONSTRAINT uq_cliente_cnpj  UNIQUE (cnpj),
    CONSTRAINT uq_cliente_email UNIQUE (email),
    CONSTRAINT ck_cliente_cpf   CHECK (CHAR_LENGTH(cpf) = 11)
) ENGINE = InnoDB;
