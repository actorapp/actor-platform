create table client_stats(
    id bigint not null,
    user_id int not null,
    auth_id bigint not null,
    event varchar(1024) not null,
    primary key(id)
);
create index on client_stats(user_id);
create index on client_stats(auth_id);