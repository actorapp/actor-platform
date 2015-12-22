alter table sticker_data add column image_128_width int not null default 128;
alter table sticker_data add column image_128_height int not null default 128;

alter table sticker_data add column image_256_width int default 256;
alter table sticker_data add column image_256_height int default 256;

alter table sticker_data add column image_512_width int default 512;
alter table sticker_data add column image_512_height int default 512;