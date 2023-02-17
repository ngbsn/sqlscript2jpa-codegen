-- create database
drop database if exists greenfall;
create database greenfall;

-- connect to database
\c greenfall;

-- create schema
drop schema if exists greenfall;
create schema greenfall;

-- create user table
drop table if exists greenfall.user;
create table greenfall.user (

);

create index idx_name on greenfall.user (name desc);