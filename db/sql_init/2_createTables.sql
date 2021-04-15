-- All tables has
-- id: primary_key, int, for join requests and foreign keys, should not be exposed to service clients
-- uri: unique identifier, immutable, can be exposed
-- creation_datetime: for diagnostics

-- 1) destination

CREATE TABLE datatoggle_server.destination_def(
    id SERIAL PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TYPE datatoggle_server.PARAM_DEF_TYPE AS ENUM ('STRING', 'INT', 'FLOAT', 'BOOLEAN');

CREATE TABLE datatoggle_server.destination_param_def(
    id SERIAL PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    type datatoggle_server.PARAM_DEF_TYPE NOT NULL,
    destination_def_id INT NOT NULL REFERENCES datatoggle_server.destination_def(id),
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2) customer

CREATE TABLE datatoggle_server.customer(
    id SERIAL PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE,
    userApiKey TEXT NOT NULL UNIQUE, -- for connection from user
    firebase_auth_uid TEXT NOT NULL UNIQUE, -- for connection from customer dashboard
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE datatoggle_server.customer_destination(
    id SERIAL PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL,
    customer_id INT NOT NULL REFERENCES datatoggle_server.customer(id),
    destination_def_id INT NOT NULL REFERENCES datatoggle_server.destination_def(id),
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE datatoggle_server.customer_destination_param(
    id SERIAL PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE,
    value_string TEXT,
    value_int INTEGER,
    value_float  DOUBLE PRECISION,
    value_boolean BOOLEAN,
    customer_destination_id INT NOT NULL REFERENCES datatoggle_server.customer_destination(id),
    destination_param_def_id INT NOT NULL REFERENCES datatoggle_server.destination_param_def(id),
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3) user

CREATE TABLE datatoggle_server.user(
    id SERIAL PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE, -- internal, immutable uri of a user
    user_id_for_customer TEXT NOT NULL, -- useId or anonymousId sent with identify call
    last_connection TIMESTAMP NOT NULL,
    customer_id INT NOT NULL REFERENCES datatoggle_server.customer(id),
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);




