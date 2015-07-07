update users set created_at = '2015-04-01 12:00:00' where created_at is null;
alter table users alter column created_at set not null;