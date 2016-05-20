alter table apple_push_credentials drop constraint apple_push_credentials_pkey;
alter table apple_push_credentials add primary key(auth_id, is_voip);