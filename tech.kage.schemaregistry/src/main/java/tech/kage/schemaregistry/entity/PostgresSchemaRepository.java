/*
 * Copyright (c) 2025, Dariusz Szpakowski
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package tech.kage.schemaregistry.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import reactor.core.publisher.Flux;

/**
 * A PostgreSQL-based implementation of {@link SchemaRepository}.
 * 
 * @author Dariusz Szpakowski
 */
@Component
class PostgresSchemaRepository implements SchemaRepository {
    private static final String SELECT_SCHEMAS_SQL = """
                SELECT
                    sub.subject, sub.version, sub.schema_id, schema,
                    ref.name AS ref_name, ref.subject AS ref_subject, ref.version AS ref_version
                FROM schemas.subjects sub
                    JOIN schemas.schemas s ON sub.schema_id = s.id
                    LEFT OUTER JOIN schemas.references ref ON s.id = ref.schema_id
                WHERE sub.subject = :subject
            """;

    private static final String SELECT_SCHEMAS_ORDER_CLAUSE_SQL = " ORDER BY sub.version DESC, ref_name";

    private static final String SUBJECT = "subject";
    private static final String VERSION = "version";
    private static final String SCHEMA_ID = "schema_id";
    private static final String SCHEMA = "schema";
    private static final String REFERENCE_NAME = "ref_name";
    private static final String REFERENCE_SUBJECT = "ref_subject";
    private static final String REFERENCE_VERSION = "ref_version";

    private final DatabaseClient databaseClient;

    /**
     * Constructs a new {@link PostgresSchemaRepository} instance.
     *
     * @param databaseClient an instance of {@link DatabaseClient}
     */
    PostgresSchemaRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<Schema> findBySubjectAndVersionOrderedByVersionDesc(String subject, Integer version) {
        var parameters = new HashMap<String, Object>();

        parameters.put(SUBJECT, subject);

        if (version != null) {
            parameters.put(VERSION, version);
        }

        return databaseClient
                .sql(SELECT_SCHEMAS_SQL
                        + (version != null ? " AND sub.version = :version" : "")
                        + SELECT_SCHEMAS_ORDER_CLAUSE_SQL)
                .bindValues(parameters)
                .fetch()
                .all()
                .bufferUntilChanged(result -> result.get(SCHEMA_ID))
                .map(this::toSchema);
    }

    /**
     * Maps database rows to a Schema object with references.
     *
     * @param rows the rows containing schema and reference data
     * 
     * @return a Schema instance
     */
    private Schema toSchema(List<Map<String, Object>> rows) {
        var references = rows
                .stream()
                .filter(row -> row.get(REFERENCE_NAME) != null)
                .map(row -> new SchemaReference(
                        (String) row.get(REFERENCE_NAME),
                        (String) row.get(REFERENCE_SUBJECT),
                        (Integer) row.get(REFERENCE_VERSION)))
                .toList();

        var subject = (String) rows.getFirst().get(SUBJECT);
        var version = (Integer) rows.getFirst().get(VERSION);
        var schemaId = (Integer) rows.getFirst().get(SCHEMA_ID);
        var schemaContent = (String) rows.getFirst().get(SCHEMA);

        return new Schema(subject, version, schemaId, "AVRO", references, schemaContent);
    }
}
