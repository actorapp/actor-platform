CREATE TABLE history_messages (
       user_id int NOT NULL,
       peer_type int NOT NULL,
       peer_id int NOT NULL,
       date timestamp NOT NULL,
       sender_user_id int NOT NULL,
       random_id bigint NOT NULL,
       message_content_header int NOT NULL,
       message_content_data bytea NOT NULL,
       deleted_at timestamp,
       PRIMARY KEY(user_id, peer_type, peer_id, date, sender_user_id, random_id)
);

CREATE TABLE dialogs (
       user_id int NOT NULL,
       peer_type int NOT NULL,
       peer_id int NOT NULL,
       last_message_date timestamp NOT NULL,
       last_received_at timestamp NOT NULL,
       last_read_at timestamp NOT NULL,
       PRIMARY KEY(user_id, peer_type, peer_id)
);

CREATE TABLE google_push_credentials (
       auth_id bigint NOT NULL,
       project_id bigint NOT NULL,
       reg_id varchar(255) NOT NULL,
       PRIMARY KEY (auth_id)
);

CREATE INDEX idx_google_push_credentials_reg_id on google_push_credentials(reg_id);

CREATE TABLE log_events (
    id serial not null,
    auth_id bigint not null,
    phone_number bigint not null,
    email varchar(255) NOT NULL,
    klass smallint NOT NULL,
    json_body text NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);

CREATE INDEX idx_log_events_auth_id ON log_events (auth_id);
CREATE INDEX idx_log_events_phone_number ON log_events (phone_number);
CREATE INDEX idx_log_events_email ON log_events (email);

CREATE TABLE apple_push_credentials (
       auth_id bigint NOT NULL,
       apns_key int NOT NULL,
       token varchar(128) NOT NULL,
       PRIMARY KEY (auth_id)
);

CREATE INDEX idx_apple_push_credentials_token on apple_push_credentials(token);

CREATE TABLE groups (
       id int NOT NULL,
       creator_user_id int NOT NULL,
       access_hash bigint NOT NULL,
       title varchar(255) NOT NULL,
       created_at timestamp NOT NULL,
       title_changer_user_id int NOT NULL,
       title_changed_at timestamp NOT NULL,
       title_change_random_id bigint NOT NULL,
       avatar_changer_user_id int NOT NULL,
       avatar_changed_at timestamp NOT NULL,
       avatar_change_random_id bigint NOT NULL,
       PRIMARY KEY (id)
);

CREATE TABLE group_users (
       group_id int NOT NULL,
       user_id int NOT NULL,
       inviter_user_id int NOT NULL,
       invited_at timestamp NOT NULL,
       PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_group_users_user_id on group_users (user_id);

CREATE TABLE unregistered_contacts (
       phone_number bigint,
       owner_user_id int,
       PRIMARY KEY (phone_number, owner_user_id)
);

CREATE TABLE user_contacts (
       owner_user_id int NOT NULL,
       contact_user_id int NOT NULL,
       phone_number bigint NOT NULL,
       name varchar(255),
       access_salt varchar(255) NOT NULL,
       is_deleted boolean NOT NULL default false,
       PRIMARY KEY (owner_user_id, contact_user_id)
);

CREATE INDEX idx_user_contacts_owner_user_id_is_deleted on user_contacts(owner_user_id, is_deleted);

CREATE TABLE file_datas (
       id bigint NOT NULL,
       access_salt varchar(255) NOT NULL,
       uploaded_blocks_count int NOT NULL default 0,
       length bigint NOT NULL default 0,
       PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS akka_journal (
       persistence_id VARCHAR(255) NOT NULL,
       sequence_number BIGINT NOT NULL,
       marker VARCHAR(255) NOT NULL,
       message TEXT NOT NULL,
       created TIMESTAMP NOT NULL,
       PRIMARY KEY(persistence_id, sequence_number)
);

CREATE TABLE IF NOT EXISTS akka_snapshot (
       persistence_id VARCHAR(255) NOT NULL,
       sequence_nr BIGINT NOT NULL,
       snapshot TEXT NOT NULL,
       created BIGINT NOT NULL,
       PRIMARY KEY (persistence_id, sequence_nr)
);

CREATE TABLE user_emails (
       id int NOT NULL,
       user_id int NOT NULL,
       email varchar(255) NOT NULL,
       access_salt varchar(255) NOT NULL,
       title varchar(255) NOT NULL,
       PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_user_emails_email ON user_emails (email);
CREATE INDEX idx_user_emails_user_id ON user_emails (user_id);

CREATE TABLE plain_mails (
       id serial not null,
       random_id bigint not null,
       mail_from varchar(255) NOT NULL,
       recipients text NOT NULL,
       message text NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT now(),
       PRIMARY KEY (id)
);

CREATE TABLE file_blocks (
       file_id bigint NOT NULL,
       offset_ bigint NOT NULL,
       length bigint NOT NULL,
       PRIMARY KEY (file_id, offset_, length)
);

CREATE TABLE auth_ids (
    id bigint NOT NULL,
    public_key_hash bigint,
    user_id int,
    deleted_at timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE users (
       id int NOT NULL,
       access_salt text NOT NULL,
       name varchar(255) NOT NULL,
       country_code varchar(2) NOT NULL,
       sex int NOT NULL,
       state int NOT NULL,
       PRIMARY KEY (id)
);

CREATE TABLE avatar_datas (
       entity_id bigint NOT NULL,
       entity_type int NOT NULL,
       small_avatar_file_id bigint,
       small_avatar_file_hash bigint,
       small_avatar_file_size int,
       large_avatar_file_id bigint,
       large_avatar_file_hash bigint,
       large_avatar_file_size int,
       full_avatar_file_id bigint,
       full_avatar_file_hash bigint,
       full_avatar_file_size int,
       full_avatar_width int,
       full_avatar_height int,
       PRIMARY KEY (entity_id, entity_type)
);

CREATE TABLE public_keys (
       user_id int NOT NULL,
       hash bigint NOT NULL,
       data bytea NOT NULL,
       deleted_at timestamp,
       PRIMARY KEY (user_id, hash)
);

CREATE TABLE auth_sessions (
       user_id int NOT NULL,
       id int NOT NULL,
       app_id int NOT NULL,
       app_title varchar(64) NOT NULL,
       auth_id bigint NOT NULL,
       public_key_hash bigint NOT NULL,
       device_hash bytea NOT NULL,
       device_title varchar(64) NOT NULL,
       auth_time timestamp NOT NULL,
       auth_location varchar(255),
       latitude double precision,
       longitude double precision,
       deleted_at timestamp,
       PRIMARY KEY (user_id, id)
);

CREATE UNIQUE INDEX ON auth_sessions(user_id, id, device_hash);

CREATE TABLE user_phones (
  user_id int NOT NULL,
  id int NOT NULL,
  access_salt varchar(255) NOT NULL,
  number bigint NOT NULL,
  title varchar(64) NOT NULL,
  PRIMARY KEY (user_id, id)
);

CREATE TABLE auth_sms_codes (
       phone_number bigint NOT NULL,
       sms_hash varchar(64) NOT NULL,
       sms_code varchar(8) NOT NULL,
       PRIMARY KEY (phone_number)
);

CREATE UNIQUE INDEX user_phones_number_idx ON user_phones (number);
