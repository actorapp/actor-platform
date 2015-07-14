create table dashboard_sessions (
    id bigint not null,
    user_id int not null,
    passcode varchar(255) not null,
    auth_token varchar(255) not null,
    is_active boolean not null,
    created_at timestamp not null,
    primary key(id)
);
create index on dashboard_sessions(user_id);
