alter table history_messages add column quoted_peer_type int default 0;
alter table history_messages add column quoted_peer_id int default 0;
alter table history_messages add column quoted_random_id bigint default 0;