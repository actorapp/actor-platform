create table sticker_packs (
    id int not null,
    access_salt varchar(255) not null,
    owner_user_id int not null,
    is_default boolean not null default false,
    primary key(id)
);

create table own_sticker_packs (
    user_id int not null,
    pack_id int not null,
    primary key(user_id, pack_id)
);

create table sticker_data (
    id int not null,
    pack_id int not null,
    emoji varchar(16),
    image_128_file_id bigint not null,
    image_128_file_hash bigint not null,
    image_128_file_size bigint not null,
    image_256_file_id bigint,
    image_256_file_hash bigint,
    image_256_file_size bigint,
    image_512_file_id bigint,
    image_512_file_hash bigint,
    image_512_file_size bigint,
    primary key(id, pack_id)
);