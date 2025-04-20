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

package tech.kage.schemaregistry.replicator.entity;

import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Confluent Schema Registry compatible implementation of
 * {@link KafkaSchemaRepository}.
 * 
 * @author Dariusz Szpakowski
 */
@Component
class ConfluentSchemaRepository implements KafkaSchemaRepository {
    static final String SCHEMAS_TOPIC = "_schemas";

    private final Converter<Schema, ProducerRecord<SchemaKey, SchemaValue>> schemaConverter;
    private final KafkaSender<SchemaKey, SchemaValue> kafkaSender;

    /**
     * Constructs a new {@link ConfluentSchemaRepository} instance.
     *
     * @param schemaConverter an instance of {@link Converter}
     * @param kafkaSender     an instance of {@link KafkaSender}
     */
    ConfluentSchemaRepository(
            Converter<Schema, ProducerRecord<SchemaKey, SchemaValue>> schemaConverter,
            KafkaSender<SchemaKey, SchemaValue> kafkaSender) {
        this.schemaConverter = schemaConverter;
        this.kafkaSender = kafkaSender;
    }

    @Override
    public Mono<Schema> save(Schema schema) {
        return Mono
                .just(schema)
                .map(schemaConverter::convert)
                .map(producerRecord -> SenderRecord.create(producerRecord, null))
                .map(Mono::just)
                .flatMapMany(kafkaSender::send)
                .single()
                .flatMap(senderResult -> Mono.justOrEmpty(senderResult.exception()))
                .flatMap(Mono::error)
                .thenReturn(schema);
    }

    /**
     * Converts {@link Schema} objects to Confluent Schema Registry compatible Kafka
     * {@link ProducerRecord}s for schema replication.
     */
    @Component
    static class ConfluentSchemaConverter implements Converter<Schema, ProducerRecord<SchemaKey, SchemaValue>> {
        @Override
        public ProducerRecord<SchemaKey, SchemaValue> convert(Schema schema) {
            return new ProducerRecord<>(SCHEMAS_TOPIC, SchemaKey.from(schema), SchemaValue.from(schema));
        }
    }

    /**
     * Kafka key for a schema record.
     *
     * @param keytype the type of key (e.g., "SCHEMA")
     * @param subject the schema subject
     * @param version the schema version
     * @param magic   the magic byte
     */
    static record SchemaKey(String keytype, String subject, int version, int magic) {
        private static SchemaKey from(Schema schema) {
            return new SchemaKey("SCHEMA", schema.getSubject(), schema.getVersion(), 1);
        }
    }

    /**
     * Kafka value for a schema record.
     *
     * @param subject    the schema subject
     * @param version    the schema version
     * @param id         the schema ID
     * @param references the schema references
     * @param schema     the schema content
     * @param deleted    whether the schema is deleted
     */
    static record SchemaValue(String subject, int version, int id, List<SchemaReference> references,
            String schema, boolean deleted) {
        private static SchemaValue from(Schema schema) {
            return new SchemaValue(schema.getSubject(), schema.getVersion(), schema.getId(), schema.getReferences(),
                    schema.getSchema(), false);
        }
    }
}
