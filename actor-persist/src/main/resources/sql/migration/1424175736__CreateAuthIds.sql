CREATE TABLE auth_ids (
    id bigint NOT NULL,
    user_id int,
    deleted_at timestamp,
    PRIMARY KEY (id)
);
