alter table users add column nickname varchar(255);
create unique index on users(nickname);
alter table users add column about varchar(255);