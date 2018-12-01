create table genre (
 genre_id int primary key,
 genre_name varchar(30),
 genre_level int 
);
alter table genre convert to character set utf8mb4;