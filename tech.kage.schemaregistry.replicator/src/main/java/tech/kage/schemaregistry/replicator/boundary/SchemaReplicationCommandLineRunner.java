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

package tech.kage.schemaregistry.replicator.boundary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import tech.kage.schemaregistry.replicator.control.SchemaReplication;

/**
 * Command-line runner to initiate schema replication to Confluent Schema
 * Registry.
 * 
 * @author Dariusz Szpakowski
 */
@Component
public class SchemaReplicationCommandLineRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaReplicationCommandLineRunner.class);

    private final SchemaReplication schemaReplication;

    public SchemaReplicationCommandLineRunner(SchemaReplication schemaReplication) {
        this.schemaReplication = schemaReplication;
    }

    /**
     * Initiates schema replication to the Confluent Schema Registry.
     *
     * @param args command-line arguments (unused)
     */
    @Override
    public void run(String... args) {
        log.info("Starting schema replication...");

        schemaReplication
                .replicateAllSchemas()
                .doOnNext(schema -> log.info("Replicated schema ID: {} for {} v{}", schema.getId(), schema.getSubject(),
                        schema.getVersion()))
                .blockLast();

        log.info("Schema replication completed successfully.");
    }
}
