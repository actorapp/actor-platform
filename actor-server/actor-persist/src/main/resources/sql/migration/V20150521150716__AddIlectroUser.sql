CREATE TABLE ilectro_users (
  user_id int NOT NULL,
  uuid UUID NOT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (user_id)
);

CREATE UNIQUE INDEX on ilectro_users(uuid);