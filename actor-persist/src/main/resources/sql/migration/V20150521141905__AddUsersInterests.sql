CREATE TABLE users_interests (
  user_id int NOT NULL,
  interest_id int NOT NULL,
  PRIMARY KEY (user_id, interest_id)
);