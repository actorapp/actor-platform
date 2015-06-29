create table oauth2_tokens (
    id bigint not null,
    user_id varchar(255) not null,
    access_token varchar(255) not null,
    token_type varchar(255) not null,
    expires_in bigint not null,
    refresh_token varchar(255),
    created_at timestamp not null,
    primary key(id, user_id)
);