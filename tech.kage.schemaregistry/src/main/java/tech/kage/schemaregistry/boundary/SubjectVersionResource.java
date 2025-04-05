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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import reactor.core.publisher.Mono;
import tech.kage.schemaregistry.control.SchemaRetrieval;

/**
 * REST resource for schema retrieval operations by subject.
 * 
 * @author Dariusz Szpakowski
 */
@RestController
@RequestMapping(value = "/subjects/{subject}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
class SubjectVersionResource {
    private final SchemaRetrieval schemaRetrieval;

    /**
     * Constructs a new {@link SubjectVersionResource} instance.
     *
     * @param schemaRetrieval an instance of {@link SchemaRetrieval}
     */
    SubjectVersionResource(SchemaRetrieval schemaRetrieval) {
        this.schemaRetrieval = schemaRetrieval;
    }

    /**
     * Retrieves the schema for the given subject and version.
     *
     * @param subject the subject for which to retrieve the schema
     * @param version "latest" for the highest version or a numeric string for a
     *                specific version
     * 
     * @return a Mono containing a ResponseEntity with the schema if found, or a 404
     *         Not Found response if none exists
     */
    @GetMapping("{version}")
    Mono<ResponseEntity<Schema>> getSchemaByVersion(@PathVariable String subject, @PathVariable String version) {
        return Mono
                .just("latest".equalsIgnoreCase(version))
                .flatMap(latestRequested -> latestRequested
                        ? schemaRetrieval.getLatestSchemaBySubject(subject)
                        : schemaRetrieval.getSchemaBySubjectAndVersion(subject, Integer.parseInt(version)))
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(notFound().build()));
    }
}
