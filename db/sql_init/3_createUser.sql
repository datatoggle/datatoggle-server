-- TODO: security; restrict rights to select/update/delete/insert

CREATE USER datatoggle_server_db_user PASSWORD 'test_datatoggle_server_db_user';
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA datatoggle_server TO datatoggle_server_db_user;
GRANT ALL PRIVILEGES ON SCHEMA datatoggle_server TO datatoggle_server_db_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA datatoggle_server TO datatoggle_server_db_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA datatoggle_server TO datatoggle_server_db_user;
