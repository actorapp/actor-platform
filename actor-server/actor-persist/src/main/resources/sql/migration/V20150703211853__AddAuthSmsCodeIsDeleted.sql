alter table auth_codes add column is_deleted boolean not null default false;
create index on auth_codes(transaction_hash, is_deleted);

alter table auth_sms_codes_obsolete add column is_deleted boolean not null default false;
create sequence tmp_seq;
alter table auth_sms_codes_obsolete add column id bigint not null default nextval('tmp_seq');
alter table auth_sms_codes_obsolete alter column id drop default;
drop sequence tmp_seq;
alter table auth_sms_codes_obsolete drop constraint auth_sms_codes_pkey;
alter table auth_sms_codes_obsolete add primary key(id);
create index on auth_sms_codes_obsolete(phone_number, is_deleted);
