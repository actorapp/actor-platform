alter table groups add column topic varchar(255);
alter table groups rename column description to about;
alter table groups alter column about drop not null;
alter table groups alter column about drop default;
update groups set about = null where about = '';