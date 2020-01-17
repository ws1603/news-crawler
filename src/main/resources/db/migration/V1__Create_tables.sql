create table news (
id bigint primary key auto_increment,
title text,
content text,
url varchar(500),
created_at timestamp default now(),
modified_at timestamp default now()
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table links_to_be_processed (link varchar(500))ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
create table links_already_processed (link varchar(500))ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


