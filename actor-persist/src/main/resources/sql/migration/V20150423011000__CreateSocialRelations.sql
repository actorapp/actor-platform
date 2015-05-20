CREATE TABLE social_relations (
  user_id int NOT NULL,
  related_to int NOT NULL,
  PRIMARY KEY (user_id, related_to)
)