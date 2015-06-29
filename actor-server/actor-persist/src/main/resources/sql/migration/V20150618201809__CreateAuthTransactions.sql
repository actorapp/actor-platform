create table auth_transactions (
    transaction_hash varchar(255) not null,
    app_id int not null,
    api_key varchar(255) not null,
    device_hash bytea not null,
    device_title varchar(255) not null,
    access_salt varchar(255) not null,
    is_checked boolean not null,
    deleted_at timestamp,
    primary key(transaction_hash)
);

create table auth_phone_transactions (
    phone_number bigint not null,
    primary key(transaction_hash)
) inherits(auth_transactions);

create table auth_email_transactions (
    email varchar(255) not null,
    redirect_uri varchar(255),
    primary key(transaction_hash)
) inherits(auth_transactions);