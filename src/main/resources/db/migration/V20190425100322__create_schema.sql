CREATE TABLE plugin (
  id SERIAL NOT NULL PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  port INT NOT NULL,
  plugin_id VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  image VARCHAR(128) NOT NULL,
  version VARCHAR(10) NOT NULL,
  env_variables TEXT NOT NULL,
  external_services TEXT NOT NULL,
  CONSTRAINT uk_plugin_version UNIQUE (plugin_id, version)
);