alter table client_stats add column event_type varchar(255) not null;
alter table client_stats rename column event to event_data;