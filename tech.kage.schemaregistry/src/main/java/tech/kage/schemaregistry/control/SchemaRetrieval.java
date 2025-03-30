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
import tech.kage.schemaregistry.entity.SchemaRepository;

/**
 * Implementation of the Schema Retrieval use case.
 * 
 * @author Dariusz Szpakowski
 */
@Component
public class SchemaRetrieval {
    private final SchemaRepository schemaRepository;

    SchemaRetrieval(SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    /**
     * Retrieves the latest schema for the specified subject, based on version
     * ordering.
     *
     * @param subject the subject for which to retrieve the latest schema
     * 
     * @return a Mono containing the schema with the highest version, or an empty
     *         Mono if none exists
     */
    public Mono<Schema> getLatestSchemaBySubject(String subject) {
        return schemaRepository
                .findBySubjectOrderedByVersionDesc(subject)
                .next();
    }
}
