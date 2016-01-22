create table dialog_commons(
    dialog_id varchar(255) not null,
    last_message_date timestamp not null,
    last_received_at timestamp not null,
    last_read_at timestamp not null,
    created_at timestamp not null,
    primary key(dialog_id)
);

-- remove dialogs with self, since we don't allow it
delete from dialogs where peer_type = 1 and peer_id = user_id;

-- remove dialogs for non-existing users
delete from dialogs where peer_type = 1 and peer_id not in(select id from users);
delete from dialogs where peer_type = 1 and user_id not in(select id from users);

-- remove dialogs for non-existing groups
delete from dialogs where peer_type = 2 and peer_id not in(select id from groups);

-- insert all common parts from dialogs to dialog_commons
insert into dialog_commons(dialog_id, last_message_date, last_received_at, last_read_at, created_at)
select distinct on(id) (concat(peer_type, '_', (case
                                when peer_type = 1 then (case
                                    when user_id < peer_id then
                                            concat(user_id, '_', peer_id)
                                        else concat(peer_id, '_', user_id)
                                    end)
                                when peer_type = 2 then peer_id::varchar
                             end))) as id, last_message_date, last_received_at, last_read_at, created_at
                             from dialogs;

-- drop common parts from dialogs
alter table dialogs drop column last_message_date;
alter table dialogs drop column last_received_at;
alter table dialogs drop column last_read_at;
alter table dialogs drop column created_at;

alter table dialogs rename to user_dialogs;