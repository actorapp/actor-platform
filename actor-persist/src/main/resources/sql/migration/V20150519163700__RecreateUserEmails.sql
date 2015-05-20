DROP TABLE user_emails;

CREATE TABLE user_emails (
       id int NOT NULL,
       user_id int NOT NULL,
       email varchar(255) NOT NULL,
       access_salt varchar(255) NOT NULL,
       title varchar(255) NOT NULL,
       PRIMARY KEY (user_id, id)
);