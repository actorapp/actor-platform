alter table user_contacts rename to user_phone_contacts;
drop index idx_user_contacts_owner_user_id_is_deleted;
alter table user_phone_contacts drop constraint user_contacts_pkey;
alter table user_phone_contacts add primary key (owner_user_id, contact_user_id);

create table user_contacts (
   owner_user_id int not null,
   contact_user_id int not null,
   name varchar(255),
   access_salt varchar(255) not null,
   is_deleted boolean not null default false,
   primary key (owner_user_id, contact_user_id)
);
create INDEX idx_user_contacts_owner_user_id_is_deleted on user_contacts(owner_user_id, is_deleted);

alter table user_phone_contacts inherit user_contacts;

create table user_email_contacts (
    email varchar(255),
    primary key(owner_user_id, contact_user_id)
) inherits(user_contacts);