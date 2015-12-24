CREATE TABLE http_api_tokens (
  token text NOT NULL,
  is_admin BOOL NOT NULL DEFAULT FALSE,
  PRIMARY KEY(token)
);