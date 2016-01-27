create table dialog_commons(
    dialog_id varchar(255) not null,
    last_message_date timestamp not null,
    last_received_at timestamp not null,
    last_read_at timestamp not null,
    primary key(dialog_id)
);

-- remove dialogs with self, since we don't allow it
delete from dialogs where peer_type = 1 and peer_id = user_id;

-- remove dialogs for non-existing users
delete from dialogs where peer_type = 1 and peer_id not in(select id from users);
delete from dialogs where peer_type = 1 and user_id not in(select id from users);

-- remove dialogs for non-existing groups
delete from dialogs where peer_type = 2 and peer_id not in(select id from groups);

-- auxiliary table to keep last message/receive/read dates. Drop when possible
create table max_dates(
    dialog_id varchar(255),
    last_message_date timestamp,
    last_received_at timestamp ,
    last_read_at timestamp,
    primary key(dialog_id)
);

-- fill it with dialog ids
insert into max_dates(dialog_id)
select distinct on(id) (concat(peer_type, '_', (case
                                when peer_type = 1 then (case
                                    when user_id < peer_id then
                                            concat(user_id, '_', peer_id)
                                        else concat(peer_id, '_', user_id)
                                    end)
                                when peer_type = 2 then peer_id::varchar
                             end))) as id from dialogs;

-- fill max dates from dialog table
with lmd as (select concat('2', '_', peer_id) as dialog_id, max(last_message_date) as last_message_date from dialogs where peer_type = 2 group by peer_id)
    update max_dates md set last_message_date = (select last_message_date from lmd where lmd.dialog_id = md.dialog_id) where dialog_id in (select dialog_id from lmd);

with lrc as (select concat('2', '_', peer_id) as dialog_id, max(last_received_at) as last_received_at from dialogs where peer_type = 2 group by peer_id)
    update max_dates md set last_received_at = (select last_received_at from lrc where lrc.dialog_id = md.dialog_id) where dialog_id in (select dialog_id from lrc);

with lrd as (select concat('2', '_', peer_id) as dialog_id, max(last_read_at) as last_read_at from dialogs where peer_type = 2 group by peer_id)
    update max_dates md set last_read_at = (select last_read_at from lrd where lrd.dialog_id = md.dialog_id) where dialog_id in (select dialog_id from lrd);

with lmd as (
    select distinct on(dialog_id) concat('1_', (case when user_id < peer_id then concat(user_id, '_', peer_id) else concat(peer_id, '_', user_id) end)) as dialog_id,
    max(last_message_date) as last_message_date from dialogs where peer_type = 1 group by dialog_id)
    update max_dates md set last_message_date = (select last_message_date from lmd where lmd.dialog_id = md.dialog_id) where dialog_id in (select dialog_id from lmd);

with lrc as (
    select distinct on(dialog_id) concat('1_', (case when user_id < peer_id then concat(user_id, '_', peer_id) else concat(peer_id, '_', user_id) end)) as dialog_id,
    max(last_received_at) as last_received_at from dialogs where peer_type = 1 group by dialog_id)
    update max_dates md set last_received_at = (select last_received_at from lrc where lrc.dialog_id = md.dialog_id) where dialog_id in (select dialog_id from lrc);

with lrd as (
    select distinct on(dialog_id) concat('1_', (case when user_id < peer_id then concat(user_id, '_', peer_id) else concat(peer_id, '_', user_id) end)) as dialog_id,
    max(last_read_at) as last_read_at from dialogs where peer_type = 1 group by dialog_id)
    update max_dates md set last_read_at = (select last_read_at from lrd where lrd.dialog_id = md.dialog_id) where dialog_id in (select dialog_id from lrd);

-- insert all common parts from dialogs to dialog_commons
insert into dialog_commons(dialog_id, last_message_date, last_received_at, last_read_at)
select distinct on(id) (concat(peer_type, '_', (case
                                when peer_type = 1 then (case
                                    when user_id < peer_id then
                                            concat(user_id, '_', peer_id)
                                        else concat(peer_id, '_', user_id)
                                    end)
                                when peer_type = 2 then peer_id::varchar
                             end))) as id, last_message_date, last_received_at, last_read_at
                             from dialogs;

-- update max dates in dialog_commons
update dialog_commons dc set last_message_date = (select md.last_message_date from max_dates md where md.dialog_id = dc.dialog_id);
update dialog_commons dc set last_received_at = (select md.last_received_at from max_dates md where md.dialog_id = dc.dialog_id);
update dialog_commons dc set last_read_at = (select md.last_read_at from max_dates md where md.dialog_id = dc.dialog_id);

-- make common parts nullable. we keep them untill we'll be sure we don't need them
alter table dialogs alter column last_message_date drop not null;
alter table dialogs alter column last_received_at drop not null;
alter table dialogs alter column last_read_at drop not null;
alter table dialogs alter column created_at drop not null;

alter table dialogs rename to user_dialogs;