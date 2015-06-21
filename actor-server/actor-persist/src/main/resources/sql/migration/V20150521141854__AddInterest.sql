CREATE TABLE ilectro_interests (
  id int NOT NULL,
  name varchar(255) NOT NULL,
  parent_id int NOT NULL,
  full_path varchar(255) NOT NULL,
  level int NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX ON ilectro_interests(level);
CREATE INDEX ON ilectro_interests(parent_id);
