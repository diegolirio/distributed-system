CREATE TABLE produto (
    id_produto INT          NOT NULL AUTO_INCREMENT,
    nome       VARCHAR(120) NOT NULL,
    categoria  ENUM('CREDITO','INVESTIMENTO','SAAS','SERVICO') NOT NULL,
    taxa_juros DECIMAL(7,4) NOT NULL DEFAULT 0.0000,
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_produto      PRIMARY KEY (id_produto),
    CONSTRAINT uq_produto_nome UNIQUE (nome),
    CONSTRAINT ck_produto_taxa CHECK (taxa_juros >= 0)
) ENGINE = InnoDB;
