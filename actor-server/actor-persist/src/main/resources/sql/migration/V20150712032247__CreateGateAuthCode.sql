create table gate_auth_codes (
    transaction_hash varchar(255) not null,
    code_hash varchar(255) not null,
    is_deleted boolean not null,
    primary key(transaction_hash)
);