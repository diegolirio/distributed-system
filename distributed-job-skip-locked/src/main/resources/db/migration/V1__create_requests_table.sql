CREATE TABLE requests (
    id            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    proposta_id   VARCHAR2(255),
    valor         NUMBER(19, 2)   NOT NULL,
    certificate   CLOB,
    status        VARCHAR2(20)    DEFAULT 'PENDING' NOT NULL,
    error_message VARCHAR2(1000),
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_requests_status ON requests (status);
