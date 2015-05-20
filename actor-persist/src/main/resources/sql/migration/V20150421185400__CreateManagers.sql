CREATE TABLE managers (
  id int NOT NULL PRIMARY KEY,
  name varchar(256) not null,
  last_name varchar(256) not null,
  domain varchar(256) not null,
  auth_token varchar(256) not null,
  email varchar(256) not null
);
CREATE UNIQUE INDEX manager_email_idx ON managers (email);