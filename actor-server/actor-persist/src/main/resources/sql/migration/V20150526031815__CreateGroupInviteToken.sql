CREATE TABLE group_invite_tokens (
  group_id INT NOT NULL,
  creator_id INT NOT NULL,
  token VARCHAR(255) NOT NULL,
  revoked_at timestamp,
  PRIMARY KEY(group_id, creator_id, token)
);
CREATE UNIQUE INDEX ON group_invite_tokens (revoked_at);
CREATE UNIQUE INDEX ON group_invite_tokens (token);