CREATE TABLE seq_updates_ngen (
  auth_id bigint NOT NULL,
  ref bytea NOT NULL,
  seq int NOT NULL,
  date timestamp NOT NULL,
  header int NOT NULL,
  serialized_data bytea NOT NULL,
  PRIMARY KEY (auth_id, ref)
);

CREATE INDEX seq_updates_auth_id_date_idx ON seq_updates_ngen(auth_id, date);
