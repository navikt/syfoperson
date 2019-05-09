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
  fnr                      VARCHAR(11)        NOT NULL,
  veileder_ident           VARCHAR(7)         NOT NULL,
  bruker_sist_aksessert    TIMESTAMP,
  enhet                    VARCHAR(4)         NOT NULL,
  opprettet                TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  sist_endret              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT VEILEDER_BEHANDLING_PK PRIMARY KEY (veileder_behandling_id),
  CONSTRAINT VEILEDER_FNR_UNIQUE UNIQUE(fnr, veileder_ident)
);
