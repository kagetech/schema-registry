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

package tech.kage.schemaregistry.control;

import org.springframework.stereotype.Component;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import reactor.core.publisher.Mono;
import tech.kage.schemaregistry.entity.RelationalSchemaRepository;

/**
 * Implementation of the Schema Lookup use case.
 * 
 * @author Dariusz Szpakowski
 */
@Component
public class SchemaLookup {
    private final RelationalSchemaRepository schemaRepository;

    /**
     * Constructs a new {@link SchemaLookup} instance.
     *
     * @param schemaRepository an instance of {@link RelationalSchemaRepository}
     */
    SchemaLookup(RelationalSchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    /**
     * Looks up a schema by subject, matching the specified schema's definition and
     * references.
     *
     * @param schema the schema to match against stored schemas
     * 
     * @return a Mono containing the matching schema with the highest version, or an
     *         empty Mono if none exists
     */
    public Mono<Schema> lookupSchema(Schema schema) {
        return schemaRepository
                .findBySubjectAndVersionOrderedByVersionDesc(schema.getSubject(), null)
                .filter(s -> s.getSchema().equals(schema.getSchema())
                        && s.getReferences().equals(schema.getReferences()))
                .singleOrEmpty();
    }
}
