
DROP TABLE IF EXISTS responses;
DROP TABLE IF EXISTS subtasks CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS types;
DROP TABLE IF EXISTS accounts CASCADE;

CREATE TABLE accounts
(
  id SERIAL PRIMARY KEY,
  email VARCHAR(100) NOT NULL,
  username VARCHAR(12) NOT NULL,
  password BIT(256) NULL,
  type BIT(3) NULL,
  session CHARACTER(32) NULL,
  last_active TIMESTAMP NULL,
  expert BOOLEAN,
  accuracy INTEGER
);

CREATE TABLE types
(
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

CREATE TABLE tasks
(
  id SERIAL PRIMARY KEY,
  submitter INTEGER REFERENCES accounts (id),
  name VARCHAR(100) NOT NULL,
  question VARCHAR(100) NOT NULL,
  accuracy INTEGER NOT NULL,
  type INTEGER REFERENCES types (id),
  ex_time TIMESTAMP,
  date_created TIMESTAMP
);

CREATE TABLE subtasks
(
  id SERIAL PRIMARY KEY,
  task INTEGER REFERENCES tasks (id),
  file_name VARCHAR(32) NOT NULL,
  active BOOLEAN
);

CREATE TABLE estimates
(
  estimate_key SERIAL PRIMARY KEY,
  subtask_id INTEGER REFERENCES subtasks (id),
  estimate BIT VARYING,
  confidence FLOAT
);

CREATE TABLE responses
(
  id SERIAL PRIMARY KEY,
  account INTEGER REFERENCES accounts (id),
  subtask INTEGER REFERENCES subtasks (id),
  response BIT VARYING NULL
);

INSERT INTO types
VALUES(1, 'binary');

INSERT INTO accounts
VALUES(1,'adam','adam', NULL, NULL, NULL, NULL, true, 1);

INSERT INTO tasks
VALUES( 1,1,'test', 'test',10,1,NULL,NULL);
