-- All tables has
-- id: primary_key, int, for join requests and foreign keys, should not be exposed to service clients
-- uri: unique identifier, immutable, can be exposed
-- creation_datetime: for diagnostics


-- 2) customer

CREATE TABLE datatoggle_server.customer(
    id SERIAL PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE,
    user_api_key UUID NOT NULL UNIQUE, -- for connection from user
    firebase_auth_uid TEXT NOT NULL UNIQUE, -- for connection from customer dashboard
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE datatoggle_server.customer_destination(
    id SERIAL PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL,
    customer_id INT NOT NULL REFERENCES datatoggle_server.customer(id),
    destination_uri TEXT NOT NULL,
    config JSONB NOT NULL,
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);


-- 3) user

CREATE TABLE datatoggle_server.user(
    id SERIAL PRIMARY KEY,
    user_uuid UUID NOT NULL UNIQUE, -- immutable datatoggle identifier of a user
    user_id_for_customer TEXT NOT NULL, -- userId or anonymousId sent with identify call
    last_connection TIMESTAMP NOT NULL,
    customer_id INT NOT NULL REFERENCES datatoggle_server.customer(id),
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);



