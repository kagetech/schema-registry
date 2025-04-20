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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.addressSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.customerProfileSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.orderSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.paymentSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.transactionSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.userSchema;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.kage.schemaregistry.entity.RelationalSchemaRepository;
import tech.kage.schemaregistry.replicator.entity.KafkaSchemaRepository;

/**
 * Specification of the Schema Replication use case.
 *
 * @author Dariusz Szpakowski
 */
@ExtendWith(MockitoExtension.class)
class SchemaReplicationTest {
    // UUT
    SchemaReplication schemaReplication;

    @Mock
    RelationalSchemaRepository relationalSchemaRepository;

    @Mock
    KafkaSchemaRepository kafkaSchemaRepository;

    @BeforeEach
    void setUp() {
        schemaReplication = new SchemaReplication(relationalSchemaRepository, kafkaSchemaRepository);
    }

    @Test
    void replicatesAllSchemasInOrder() {
        // Given
        var allSchemas = List.of(
                userSchema(1, 1001, ""),
                addressSchema(1, 1002, ""),
                orderSchema(1, 1003, ""),
                paymentSchema(1, 1004, ""),
                customerProfileSchema(1, 1005, ""),
                transactionSchema(1, 1006, ""),
                paymentSchema(2, 1014, "2"),
                transactionSchema(2, 1016, "2"),
                addressSchema(2, 1022, "2"),
                orderSchema(2, 1023, "2"),
                paymentSchema(3, 1024, "3"),
                transactionSchema(3, 1026, "3"),
                transactionSchema(4, 1036, "4"));

        given(relationalSchemaRepository.findAllOrderedBySchemaId())
                .willReturn(Flux.fromIterable(allSchemas));

        given(kafkaSchemaRepository.save(any(Schema.class)))
                .willAnswer(inv -> Mono.just(inv.getArguments()[0]));

        // When
        var replicationResult = schemaReplication.replicateAllSchemas();

        // Then
        StepVerifier
                .create(replicationResult)
                .expectNextSequence(allSchemas)
                .as("replicates all schemas successfully")
                .verifyComplete();

        var schemaCaptor = ArgumentCaptor.forClass(Schema.class);

        verify(kafkaSchemaRepository, times(allSchemas.size())).save(schemaCaptor.capture());

        assertThat(schemaCaptor.getAllValues())
                .describedAs("replicated schemas")
                .isEqualTo(allSchemas);
    }
}
