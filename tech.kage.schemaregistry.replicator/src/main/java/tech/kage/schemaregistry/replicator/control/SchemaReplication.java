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

package tech.kage.schemaregistry.replicator.control;

import org.springframework.stereotype.Component;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import reactor.core.publisher.Flux;
import tech.kage.schemaregistry.entity.RelationalSchemaRepository;
import tech.kage.schemaregistry.replicator.entity.KafkaSchemaRepository;

/**
 * Implementation of the Schema Replication use case.
 * 
 * @author Dariusz Szpakowski
 */
@Component
public class SchemaReplication {
    private final RelationalSchemaRepository relationalSchemaRepository;
    private final KafkaSchemaRepository kafkaSchemaRepository;

    /**
     * Constructs a new {@link SchemaReplication} instance.
     *
     * @param relationalSchemaRepository an instance of
     *                                   {@link RelationalSchemaRepository}
     * @param kafkaSchemaRepository      an instance of
     *                                   {@link KafkaSchemaRepository}
     */
    public SchemaReplication(
            RelationalSchemaRepository relationalSchemaRepository,
            KafkaSchemaRepository kafkaSchemaRepository) {
        this.relationalSchemaRepository = relationalSchemaRepository;
        this.kafkaSchemaRepository = kafkaSchemaRepository;
    }

    /**
     * Replicates all schemas to the Kafka repository in schema ID order.
     *
     * @return a {@link Flux} emitting each replicated {@link Schema}, completing
     *         when all schemas are replicated, or erroring on failure
     */
    public Flux<Schema> replicateAllSchemas() {
        return relationalSchemaRepository
                .findAllOrderedBySchemaId()
                .concatMap(kafkaSchemaRepository::save);
    }
}
