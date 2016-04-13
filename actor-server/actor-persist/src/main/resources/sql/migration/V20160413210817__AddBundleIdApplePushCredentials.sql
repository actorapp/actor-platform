alter table apple_push_credentials alter column apns_key drop not null;
alter table apple_push_credentials add column bundle_id varchar(255);