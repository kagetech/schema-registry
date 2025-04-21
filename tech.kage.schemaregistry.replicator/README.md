# PostgreSQL -> Confluent Schema Registry Schema Replicator

A lightweight schema replicator from PostgreSQL to Confluent Schema Registry’s internal `_schemas` Kafka topic.

## Getting started

Given a [database schema](../README.md#getting-started), run:

```
java -jar tech.kage.schemaregistry.replicator-1.0.0.jar --spring.r2dbc.url=r2dbc:postgresql://localhost:5432/testdb --spring.r2dbc.username=postgres --spring.r2dbc.password=postgres --spring.kafka.bootstrap-servers=localhost:9092
```

## Configuration

The list of supported configuration properties is given below.

**Database configuration**

- `spring.r2dbc.url` or `DATABASE_URL` environment variable - sets database URL (e.g. "r2dbc:postgresql://localhost:5432/testdb"),
- `spring.r2dbc.username` or `DATABASE_USERNAME` environment variable - sets database username.
- `spring.r2dbc.password` or `DATABASE_PASSWORD` environment variable - sets database password.

**Kafka configuration**

- `spring.kafka.bootstrap-servers` or `KAFKA_URL` environment variable - sets Kafka address (e.g. "localhost:9092").

## License

This project is released under the [BSD 2-Clause License](LICENSE).
