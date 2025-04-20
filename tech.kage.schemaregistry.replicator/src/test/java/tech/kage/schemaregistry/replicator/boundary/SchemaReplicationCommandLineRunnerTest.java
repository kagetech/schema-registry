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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.userSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import tech.kage.schemaregistry.replicator.control.SchemaReplication;

/**
 * Tests for {@link SchemaReplicationCommandLineRunner}, verifying schema
 * replication initiation.
 *
 * @author Dariusz Szpakowski
 */
@ExtendWith(MockitoExtension.class)
class SchemaReplicationCommandLineRunnerTest {
    // UUT
    SchemaReplicationCommandLineRunner cli;

    @Mock
    SchemaReplication schemaReplication;

    @BeforeEach
    void setUp() {
        cli = new SchemaReplicationCommandLineRunner(schemaReplication);
    }

    @Test
    void triggersReplication() {
        // Given
        var schema = userSchema(1, 1001, "");

        given(schemaReplication.replicateAllSchemas())
                .willReturn(Flux.just(schema));

        // When
        cli.run();

        // Then
        verify(schemaReplication, times(1)).replicateAllSchemas();
    }

    @Test
    void throwsExceptionWhenReplicationFailed() {
        // Given
        var expectedException = new IllegalStateException("Some replication error");

        given(schemaReplication.replicateAllSchemas())
                .willReturn(Flux.error(expectedException));

        // When
        var thrown = assertThrows(Throwable.class, () -> cli.run());

        // Then
        assertThat(thrown)
                .describedAs("thrown exception")
                .isEqualTo(expectedException);
    }
}
