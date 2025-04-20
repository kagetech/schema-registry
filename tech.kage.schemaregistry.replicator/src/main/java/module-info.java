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

/**
 * Replicator of schemas from schema-registry to Confluent Schema Registry
 * Kafka-based store.
 * 
 * @author Dariusz Szpakowski
 */
module tech.kage.schemaregistry.replicator {
    requires transitive tech.kage.schemaregistry.entity;

    requires reactor.core;
    requires org.reactivestreams;

    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires spring.beans;
    requires spring.context;
    requires transitive spring.core;

    requires org.slf4j;

    // Entity
    requires spring.kafka;
    requires reactor.kafka;
    requires kafka.clients;
    requires com.fasterxml.jackson.databind;

    exports tech.kage.schemaregistry.replicator to spring.beans, spring.context;
    exports tech.kage.schemaregistry.replicator.boundary to spring.beans;
    exports tech.kage.schemaregistry.replicator.control to spring.beans;
    exports tech.kage.schemaregistry.replicator.entity to spring.beans, spring.context;

    opens tech.kage.schemaregistry.replicator to spring.core;
    opens tech.kage.schemaregistry.replicator.entity to spring.core, com.fasterxml.jackson.databind;
}
