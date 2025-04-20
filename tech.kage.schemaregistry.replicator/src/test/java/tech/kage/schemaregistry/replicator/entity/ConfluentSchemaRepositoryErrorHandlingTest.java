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

package tech.kage.schemaregistry.replicator.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.userSchema;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;
import tech.kage.schemaregistry.replicator.entity.ConfluentSchemaRepository.SchemaKey;
import tech.kage.schemaregistry.replicator.entity.ConfluentSchemaRepository.SchemaValue;

/**
 * Error handling tests of {@link ConfluentSchemaRepository}.
 *
 * @author Dariusz Szpakowski
 */
@ExtendWith(MockitoExtension.class)
class ConfluentSchemaRepositoryErrorHandlingTest {
    // UUT
    ConfluentSchemaRepository kafkaSchemaRepository;

    @Mock
    Converter<Schema, ProducerRecord<SchemaKey, SchemaValue>> schemaConverter;

    @Mock
    KafkaSender<SchemaKey, SchemaValue> kafkaSender;

    @Mock
    SenderResult<Object> senderResult;

    @BeforeEach
    void setUp() {
        kafkaSchemaRepository = new ConfluentSchemaRepository(schemaConverter, kafkaSender);
    }

    @Test
    void returnsErrorWhenKafkaUnavailable() {
        // Given
        var schema = userSchema(1, 1001, "");
        var fakeProducerRecord = new ProducerRecord<SchemaKey, SchemaValue>("topic", null, null);

        var expectedError = new IllegalStateException("Some error");

        given(schemaConverter.convert(schema))
                .willReturn(fakeProducerRecord);

        given(kafkaSender.send(any()))
                .willReturn(Flux.error(expectedError));

        // When
        var result = kafkaSchemaRepository.save(schema);

        // Then
        StepVerifier
                .create(result)
                .as("Fail when Kafka unavailable")
                .expectErrorMatches(thrown -> thrown.equals(expectedError))
                .verify();
    }

    @Test
    void returnsErrorWhenKafkaSendFails() {
        // Given
        var schema = userSchema(1, 1001, "");
        var fakeProducerRecord = new ProducerRecord<SchemaKey, SchemaValue>("topic", null, null);

        var expectedError = new IllegalStateException("Another error");

        given(schemaConverter.convert(schema))
                .willReturn(fakeProducerRecord);

        given(kafkaSender.send(any()))
                .willReturn(Flux.just(senderResult));

        given(senderResult.exception())
                .willReturn(expectedError);

        // When
        var result = kafkaSchemaRepository.save(schema);

        // Then
        StepVerifier
                .create(result)
                .as("Fail when Kafka send returned error")
                .expectErrorMatches(thrown -> thrown.equals(expectedError))
                .verify();
    }
}
