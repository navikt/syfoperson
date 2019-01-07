-- ROLLBACK-START
------------------
-- DROP TABLE VEILEDER_BEHANDLING;
-- DROP SEQUENCE VEILEDER_BEHANDLING_ID_SEQ
---------------
-- ROLLBACK-END

CREATE SEQUENCE VEILEDER_BEHANDLING_ID_SEQ;

CREATE TABLE VEILEDER_BEHANDLING (
  veileder_behandling_id   NUMBER(19, 0)      NOT NULL,
  veileder_behandling_uuid VARCHAR(50) UNIQUE NOT NULL,
  aktor_id                 VARCHAR(13)        NOT NULL,
  veileder_ident           VARCHAR(7)         NOT NULL,
  under_behandling         NUMBER CHECK (under_behandling IN (1, 0)),
  CONSTRAINT VEILEDER_BEHANDLING_PK PRIMARY KEY (veileder_behandling_id),
  CONSTRAINT AKTOR_VEILEDER_UNIQUE UNIQUE(aktor_id, veileder_ident)
);
