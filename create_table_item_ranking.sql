create table item_ranking (
    genre_id int ,
    rank int ,
    item_name varchar(255) ,
    primary key (genre_id,rank)
);
alter table item_ranking convert to character set utf8mb4;