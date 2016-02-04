ALTER TABLE user_presences DROP CONSTRAINT user_presences_pkey;
DROP INDEX user_presences_user_id_auth_id_idx;
ALTER TABLE user_presences ADD PRIMARY KEY (user_id, auth_id);