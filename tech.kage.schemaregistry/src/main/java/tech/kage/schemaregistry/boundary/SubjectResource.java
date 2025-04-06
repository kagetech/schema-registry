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

package tech.kage.schemaregistry.boundary;

import static org.springframework.http.ResponseEntity.notFound;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.RegisterSchemaRequest;
import reactor.core.publisher.Mono;
import tech.kage.schemaregistry.control.SchemaLookup;

/**
 * REST resource for schema lookups under specific subjects.
 * 
 * @author Dariusz Szpakowski
 */
@RestController
@RequestMapping(value = "/subjects", produces = MediaType.APPLICATION_JSON_VALUE)
class SubjectResource {
    private final SchemaLookup schemaLookup;

    /**
     * Constructs a new {@link SubjectResource} instance.
     *
     * @param schemaLookup an instance of {@link SchemaLookup}
     */
    SubjectResource(SchemaLookup schemaLookup) {
        this.schemaLookup = schemaLookup;
    }

    /**
     * Looks up a schema by subject, matching the specified schema's definition and
     * references from the request body.
     *
     * @param subject the subject under which to look up the schema
     * @param request the schema details to match against stored schemas
     * 
     * @return a Mono containing a ResponseEntity with the matching schema (200 OK)
     *         if found, or a 404 Not Found response if none exists
     */
    @PostMapping(path = "{subject}", consumes = "application/vnd.schemaregistry.v1+json")
    Mono<ResponseEntity<Schema>> lookUpSchemaUnderSubject(
            @PathVariable String subject,
            @RequestBody RegisterSchemaRequest request) {
        return schemaLookup
                .lookupSchema(new Schema(subject, request))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(notFound().build()));
    }
}
