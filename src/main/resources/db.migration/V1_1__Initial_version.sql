-- ROLLBACK-START
------------------
-- DROP TABLE VEILEDER_BEHANDLING;

---------------
-- ROLLBACK-END

CREATE TABLE VEILEDER_BEHANDLING (
  veileder_behandling_id   NUMBER(19, 0)      NOT NULL,
  veileder_behandling_uuid VARCHAR(50) UNIQUE NOT NULL,
  aktor_id                 VARCHAR(13)        NOT NULL,
  veileder_ident           VARCHAR(7)         NOT NULL,
  under_behandling         NUMBER CHECK (under_behandling IN (1, 0)),
  CONSTRANT                VEILEDER_BEHANDLING_PK PRIMARY KEY (veileder_behandling_id)
);