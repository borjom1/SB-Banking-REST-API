drop table if exists transactions;
drop table if exists cards;
drop table if exists card_types;
drop table if exists card_providers;
drop table if exists currency_types;
drop table if exists user_roles;
drop table if exists roles;
drop table if exists users;

-- CREDENTIALS

create table users
(
    id bigserial primary key,
    first_name varchar(20) not null,
    last_name varchar(20) not null,
    registered_at timestamptz not null,
    phone_number varchar(13) unique not null,
    ipn varchar(10) unique not null,
    password text not null,
    refresh_token text
);

create table roles
(
    id serial primary key,
    name varchar(32) unique not null
);

create table user_roles
(
    user_id bigint references users(id),
    role_id int references roles(id),
    primary key (user_id, role_id)
);

insert into roles(name) values ('ROLE_USER'), ('ROLE_ADMIN');

-- CARD ENTITIES

create table card_types
(
    id serial primary key,
    name varchar(20) unique not null
);

insert into card_types (name) values ('junior'), ('credit'), ('debit');

create table card_providers
(
    id serial primary key,
    name varchar(20) unique not null,
    code varchar(4) unique not null
);

insert into card_providers (name, code)
values ('visa', 4168), ('mastercard', 5178);

create table currency_types
(
    id serial primary key,
    name varchar(20) unique not null,
    buying_exchange_rate decimal not null,
    sales_exchange_rate decimal not null,
    commission decimal not null
);

insert into currency_types (name, buying_exchange_rate, sales_exchange_rate, commission)
values ('uah', 0.0, 0.0, 1),
       ('usd', 36.62, 37.79, 2),
       ('eur', 38.9, 39.13, 1);

create table cards
(
    id bigserial primary key,
    card_number varchar(16) unique not null,
    owner_id bigint references users(id) not null,
    created_at timestamptz not null,
    cvv_code char(3) not null,
    expiry_date date not null,
    pin_code char(4) not null,
    type_id int references card_types(id) not null,
    currency_id int references currency_types(id) not null,
    provider_id int references card_providers(id) not null,
    sum numeric(18, 2) not null default 0.0,
    sum_limit int not null default 10000,
    blocked boolean not null default false
);

create table transactions
(
    id bigserial primary key,
    sender_card_id int references cards(id) not null,
    receiver_card_id int references cards(id) not null,
    purpose text not null,
    time timestamptz not null,
    sum numeric(18, 2) not null,
    converted_sum numeric(18, 2) not null,
    commission numeric(18, 2) not null,
    converted_commission numeric(18, 2) not null
);