CREATE TABLE auth_username_transactions (
  username TEXT NOT NULL,
  user_id INT,
  PRIMARY KEY (transaction_hash)
) INHERITS(auth_transactions)