CREATE TABLE vox_users (
  user_id INT NOT NULL,
  vox_user_id BIGINT NOT NULL,
  user_name VARCHAR(255) NOT NULL,
  display_name VARCHAR(255) NOT NULL,
  salt VARCHAR(255) NOT NULL,
  PRIMARY KEY(user_id)
);

CREATE UNIQUE INDEX on vox_users(vox_user_id);