alter table unregistered_contacts rename to unregistered_phone_contacts;
alter table unregistered_phone_contacts drop constraint unregistered_contacts_pkey;
alter table unregistered_phone_contacts add primary key(phone_number, owner_user_id);

create table unregistered_contacts (
    owner_user_id int not null,
    name text,
    primary key (owner_user_id)
);

alter table unregistered_phone_contacts inherit unregistered_contacts;

create table unregistered_email_contacts (
    email varchar(255),
    primary key (owner_user_id, email)
) inherits(unregistered_contacts);