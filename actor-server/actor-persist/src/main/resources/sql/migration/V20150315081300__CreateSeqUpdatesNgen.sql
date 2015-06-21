CREATE TABLE seq_updates_ngen (
  auth_id bigint NOT NULL,
  timestamp bigint NOT NULL,
  seq int NOT NULL,
  header int NOT NULL,
  serialized_data bytea NOT NULL,
  user_ids_str text NOT NULL,
  group_ids_str text NOT NULL,
  PRIMARY KEY (auth_id, timestamp)
);
