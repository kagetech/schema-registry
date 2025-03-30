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
 * A minimal PostgreSQL-based schema registry with partial Confluent
 * compatibility.
 * 
 * @author Dariusz Szpakowski
 */
module tech.kage.schemaregistry {
    requires reactor.core;
    requires org.reactivestreams;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.context;

    requires transitive kafka.schema.registry.client;

    // Boundary
    requires spring.web;
    requires spring.webflux;

    // Entity
    requires spring.r2dbc;

    exports tech.kage.schemaregistry to spring.beans, spring.context;
    exports tech.kage.schemaregistry.boundary to spring.beans, spring.context;
    exports tech.kage.schemaregistry.entity to spring.beans, spring.context;

    opens tech.kage.schemaregistry to spring.core;
    opens tech.kage.schemaregistry.boundary to spring.core;
    opens tech.kage.schemaregistry.control to spring.core;
    opens tech.kage.schemaregistry.entity to spring.core;
}
