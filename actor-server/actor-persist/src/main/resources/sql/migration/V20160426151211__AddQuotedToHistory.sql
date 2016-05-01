alter table history_messages add column quoted_peer_type int default NULL;
alter table history_messages add column quoted_peer_id int default NULL;
alter table history_messages add column quoted_random_id bigint default NULL;