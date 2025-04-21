# PostgreSQL-based Schema Registry

A minimal, embedded [Apache Avro](https://avro.apache.org/) schema registry using [PostgreSQL](https://www.postgresql.org/) as the storage backend, with partial compatibility with [Confluent Schema Registry](https://github.com/confluentinc/schema-registry). It enables starting small with the option to migrate to the full Confluent Schema Registry later.

## Supported APIs

Partially supported Confluent Schema Registry APIs ([Schema Registry API Reference](https://docs.confluent.io/platform/current/schema-registry/develop/api.html)):

- Retrieve a schema version for a given subject
  - `GET /subjects/{subject}/versions/{version}`
  - `GET /subjects/{subject}/versions/latest`
- Verify if a schema is registered under a subject
  - `POST /subjects/{subject}`

## Getting started

**Database schema:**

```sql
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
    FOREIGN KEY (schema_id) REFERENCES schemas.schemas(id),
    FOREIGN KEY (subject, version) REFERENCES schemas.subjects
);
```

**Insert sample schemas:**

```sql
-- insert schemas
INSERT INTO schemas.schemas (id, schema) VALUES
(1001, '{"type":"record","name":"User","namespace":"com.example","fields":[{"name":"id","type":"long"},{"name":"username","type":"string"}]}'),
(1002, '{"type":"record","name":"Address","namespace":"com.example","fields":[{"name":"street","type":"string"},{"name":"city","type":"string"}]}'),
(1003, '{"type":"record","name":"Order","namespace":"com.example","fields":[{"name":"orderId","type":"long"},{"name":"user","type":"com.example.User"},{"name":"address","type":"com.example.Address"}]}');

-- map subjects to schema versions
INSERT INTO schemas.subjects (subject, version, schema_id) VALUES
('user-subject', 1, 1001),
('address-subject', 1, 1002),
('order-subject', 1, 1003);

-- define references (order-subject references user-subject and address-subject)
INSERT INTO schemas.references (schema_id, name, subject, version) VALUES
(1003, 'com.example.User', 'user-subject', 1),
(1003, 'com.example.Address', 'address-subject', 1);
```

**Maven configuration:**

```xml
<dependency>
    <groupId>tech.kage.schemaregistry</groupId>
    <artifactId>tech.kage.schemaregistry</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Schema Registry URL**

```properties
server.port=8081

# Kafka configuration
spring.kafka.properties.schema.registry.url=http://localhost:${server.port:8080}/schema-registry
```

**Replicate schemas to Confluent Schema Registry**

See [Schema Replicator](tech.kage.schemaregistry.replicator).

## License

This project is released under the [BSD 2-Clause License](LICENSE).
