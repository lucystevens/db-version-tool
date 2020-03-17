CREATE SCHEMA IF NOT EXISTS version;

DROP TABLE IF EXISTS version.version;
DROP TABLE IF EXISTS test;

CREATE TABLE version.version(version INT PRIMARY KEY);
INSERT INTO version.version VALUES(0);