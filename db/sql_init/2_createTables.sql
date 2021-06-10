-- All tables has
-- id: primary_key, int, for join requests and foreign keys, should not be exposed to service clients
-- uri: unique identifier, immutable, can be exposed
-- creation_datetime: for diagnostics


CREATE TABLE datatoggle_server.user_account(
   id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   uri TEXT NOT NULL UNIQUE,
   firebase_auth_uid TEXT NOT NULL UNIQUE, -- for connection from customer dashboard
   creation_datetime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE datatoggle_server.project(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    uri TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    creation_datetime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE datatoggle_server.project_member(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES datatoggle_server.project(id),
    user_account_id BIGINT NOT NULL REFERENCES datatoggle_server.user_account(id),
    creation_datetime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE(project_id, user_account_id)
);


CREATE TABLE datatoggle_server.project_source(
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  uri TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  api_key UUID NOT NULL UNIQUE, -- for connection from user
  project_id BIGINT NOT NULL REFERENCES datatoggle_server.project(id),
  creation_datetime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);


CREATE TABLE datatoggle_server.project_destination(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    enabled BOOLEAN NOT NULL,
    project_id BIGINT NOT NULL REFERENCES datatoggle_server.project(id),
    destination_uri TEXT NOT NULL, -- reference code
    destination_specific_config JSONB NOT NULL,
    last_modification_datetime TIMESTAMPTZ NOT NULL,
    creation_datetime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE(project_id, destination_uri) -- for now, it's not possible to have several time the same destination
);


CREATE TABLE datatoggle_server.project_connection(
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES datatoggle_server.project(id),
  source_id BIGINT NOT NULL REFERENCES datatoggle_server.project_source(id),
  destination_id BIGINT NOT NULL REFERENCES datatoggle_server.project_destination(id),
  creation_datetime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);


-- Analytics

CREATE TABLE datatoggle_server.tracked_session(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    api_key UUID NOT NULL,
    sampling INT NOT NULL,
    creation_datetime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);
