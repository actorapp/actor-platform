CREATE TABLE IF NOT EXISTS public.persistence_journal (
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  created BIGINT NOT NULL,
  tags VARCHAR(255) DEFAULT NULL,
  message BYTEA NOT NULL,
  PRIMARY KEY(persistence_id, sequence_number)
);

DROP TABLE IF EXISTS public.persistence_deleted_to;

CREATE TABLE IF NOT EXISTS public.persistence_deleted_to (
  persistence_id VARCHAR(255) NOT NULL,
  deleted_to BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS public.persistence_snapshot (
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  created BIGINT NOT NULL,
  snapshot BYTEA NOT NULL,
  PRIMARY KEY(persistence_id, sequence_number)
);