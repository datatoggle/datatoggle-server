INSERT INTO datatoggle_server.destination_def(uri, name) VALUES
('mixpanel', 'Mixpanel'),
('amplitude', 'Amplitude');


INSERT INTO datatoggle_server.destination_param_def(uri, name, type, destination_def_id) VALUES
('mixpanel_project_token', 'Project token', 'STRING', 1),
('mixpanel_eu_residency', 'EU residency', 'BOOLEAN', 1),
('amplitude_api_key', 'API key', 'STRING', 2);
