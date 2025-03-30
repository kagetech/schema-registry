CREATE SCHEMA IF NOT EXISTS schemas;

-- schemas
CREATE TABLE IF NOT EXISTS schemas.schemas (
    id integer PRIMARY KEY,
    schema text NOT NULL
);

-- subjects
CREATE TABLE IF NOT EXISTS schemas.subjects (
    subject text,
    version integer,
    schema_id integer NOT NULL REFERENCES schemas.schemas,

    PRIMARY KEY (subject, version)
);

CREATE INDEX IF NOT EXISTS subjects_schema_id_fkey ON schemas.subjects(schema_id);

-- references
CREATE TABLE IF NOT EXISTS schemas.references (
    schema_id integer,
    name text,
    subject text NOT NULL,
    version integer NOT NULL,
    
    PRIMARY KEY (schema_id, name),
    FOREIGN KEY (subject, version) REFERENCES schemas.subjects
);
