alter table users add column created_at timestamp;
with sess as (select user_id, min(auth_time) as created_at from auth_sessions group by user_id) update users set created_at = (select created_at from sess where sess.user_id = id);
update users set created_at = now() where is_bot = true;