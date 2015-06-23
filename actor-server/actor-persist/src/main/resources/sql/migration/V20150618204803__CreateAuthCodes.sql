create table auth_codes (
    transaction_hash varchar(255) not null,
    code varchar(8) not null,
    created_at timestamp not null,
    primary key(transaction_hash)
);
