CREATE TABLE files (
  id bigint NOT NULL,
  access_salt text NOT NULL,
  s3_upload_key text NOT NULL,
  is_uploaded boolean default false NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX on files(s3_upload_key);

CREATE TABLE file_parts (
  file_id bigint NOT NULL,
  number int NOT NULL,
  size int NOT NULL,
  s3_upload_key text NOT NULL,
  PRIMARY KEY (file_id, number)
)