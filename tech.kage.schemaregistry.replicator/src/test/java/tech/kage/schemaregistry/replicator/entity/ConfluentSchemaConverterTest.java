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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.addressSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.orderSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.paymentSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.transactionSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.userSchema;

import java.util.stream.Stream;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.convert.converter.Converter;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import tech.kage.schemaregistry.replicator.entity.ConfluentSchemaRepository.ConfluentSchemaConverter;
import tech.kage.schemaregistry.replicator.entity.ConfluentSchemaRepository.SchemaKey;
import tech.kage.schemaregistry.replicator.entity.ConfluentSchemaRepository.SchemaValue;

/**
 * Tests of {@link ConfluentSchemaConverter}.
 *
 * @author Dariusz Szpakowski
 */
class ConfluentSchemaConverterTest {
    // UUT
    Converter<Schema, ProducerRecord<SchemaKey, SchemaValue>> schemaConverter = new ConfluentSchemaConverter();

    @ParameterizedTest
    @MethodSource("testSchemas")
    void convertsSchemaToProducerRecord(Schema schema, SchemaKey expectedKey, SchemaValue expectedValue) {
        // When
        var producerRecord = schemaConverter.convert(schema);

        // Then
        assertThat(producerRecord.key())
                .describedAs("converted producer record key")
                .isEqualTo(expectedKey);

        assertThat(producerRecord.value())
                .describedAs("converted producer record value")
                .isEqualTo(expectedValue);
    }

    static Stream<Arguments> testSchemas() {
        var userSchema = userSchema(1, 1001, "");
        var addressSchema = addressSchema(2, 1022, "2");
        var orderSchema = orderSchema(2, 1023, "2");
        var paymentSchema = paymentSchema(3, 1024, "3");
        var transactionSchema = transactionSchema(4, 1036, "4");

        return Stream.of(
                arguments(
                        named("user schema", userSchema),
                        named("user schema message key", new SchemaKey("SCHEMA", "user-subject", 1, 1)),
                        named("user schema message value",
                                new SchemaValue("user-subject", 1, 1001, userSchema.getReferences(),
                                        userSchema.getSchema(), false))),
                arguments(
                        named("address schema", addressSchema),
                        named("address schema message key", new SchemaKey("SCHEMA", "address-subject", 2, 1)),
                        named("address schema message value",
                                new SchemaValue("address-subject", 2, 1022, addressSchema.getReferences(),
                                        addressSchema.getSchema(), false))),
                arguments(
                        named("order schema", orderSchema),
                        named("order schema message key", new SchemaKey("SCHEMA", "order-subject", 2, 1)),
                        named("order schema message value",
                                new SchemaValue("order-subject", 2, 1023, orderSchema.getReferences(),
                                        orderSchema.getSchema(), false))),
                arguments(
                        named("payment schema", paymentSchema),
                        named("payment schema message key", new SchemaKey("SCHEMA", "payment-subject", 3, 1)),
                        named("payment schema message value",
                                new SchemaValue("payment-subject", 3, 1024, paymentSchema.getReferences(),
                                        paymentSchema.getSchema(), false))),
                arguments(
                        named("transaction schema", transactionSchema),
                        named("transaction schema message key", new SchemaKey("SCHEMA", "transaction-subject", 4, 1)),
                        named("transaction schema message value",
                                new SchemaValue("transaction-subject", 4, 1036, transactionSchema.getReferences(),
                                        transactionSchema.getSchema(), false))));
    }
}
