create schema if not exists persons;
create extension if not exists pgcrypto;

create role master
nosuperuser
valid until 'infinity';

grant connect on database persons to master;

grant usage on schema persons to master;
grant select on all tables in schema persons to master;
grant select on all sequences in schema persons to master;
grant execute on all routines in schema persons to master;

alter default privileges in schema persons grant all on tables to master;
alter default privileges in schema persons grant all on functions to master;
alter default privileges in schema persons grant all on routines to master;
alter default privileges in schema persons grant all on types to master;
alter default privileges in schema persons grant all on sequences to master;
-- alter default privileges in schema persons grant all on schemas to master;
-- alter default privileges in schema persons grant all on procedures to master;

alter default privileges in schema persons grant select on tables to master;

create user backend in role master password 'backend';
