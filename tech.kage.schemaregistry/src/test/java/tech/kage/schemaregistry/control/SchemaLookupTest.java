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

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static tech.kage.schemaregistry.test.data.TestSchemas.addressSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.customerProfileSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.orderSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.paymentSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.transactionSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.userSchema;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import tech.kage.schemaregistry.entity.RelationalSchemaRepository;

/**
 * Specification of the Schema Lookup use case.
 * 
 * @author Dariusz Szpakowski
 */
class SchemaLookupTest {
    RelationalSchemaRepository schemaRepository = mock(RelationalSchemaRepository.class);

    // UUT
    SchemaLookup schemaLookup = new SchemaLookup(schemaRepository);

    @ParameterizedTest
    @MethodSource("testSchemas")
    void findsGivenSchema(Schema schema, List<Schema> schemasWithSubject, Schema expectedSchema) {
        // Given
        given(schemaRepository.findBySubjectAndVersionOrderedByVersionDesc(schema.getSubject(), null))
                .willReturn(Flux.fromIterable(schemasWithSubject));

        // When
        var foundSchema = schemaLookup.lookupSchema(schema);

        // Then
        StepVerifier
                .create(foundSchema)
                .expectNext(expectedSchema)
                .as("find expected schema")
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("notFoundTestSchemas")
    void returnsEmptyMonoWhenSchemaNotFound(Schema schema, List<Schema> schemasWithSubject) {
        // Given
        given(schemaRepository.findBySubjectAndVersionOrderedByVersionDesc(schema.getSubject(), null))
                .willReturn(Flux.fromIterable(schemasWithSubject));

        // When
        var foundSchema = schemaLookup.lookupSchema(schema);

        // Then
        StepVerifier
                .create(foundSchema)
                .as("returns empty mono when schema not found")
                .verifyComplete();
    }

    static Stream<Arguments> testSchemas() {
        return Stream.of(
                arguments(
                        named("user schema", userSchema(null, null, "")),
                        named("user schemas", List.of(userSchema(1, 1001, ""))),
                        named("user schema 1", userSchema(1, 1001, ""))),
                arguments(
                        named("address schema", addressSchema(null, null, "2")),
                        named("address schemas", List.of(addressSchema(2, 1022, "2"), addressSchema(1, 1002, ""))),
                        named("address schema 2", addressSchema(2, 1022, "2"))),
                arguments(
                        named("order schema", orderSchema(null, null, "2")),
                        named("order schemas", List.of(orderSchema(2, 1023, "2"), orderSchema(1, 1003, ""))),
                        named("order schema 2", orderSchema(2, 1023, "2"))),
                arguments(
                        named("payment schema", paymentSchema(null, null, "3")),
                        named("payment schemas",
                                List.of(paymentSchema(3, 1024, "3"),
                                        paymentSchema(2, 1014, "2"),
                                        paymentSchema(1, 1004, ""))),
                        named("payment schema 3", paymentSchema(3, 1024, "3"))),
                arguments(
                        named("customer profile schema", customerProfileSchema(null, null, "")),
                        named("customer profile schemas", List.of(customerProfileSchema(1, 1005, ""))),
                        named("customer profile schema 1", customerProfileSchema(1, 1005, ""))),
                arguments(
                        named("transaction schema", transactionSchema(null, null, "4")),
                        named("transaction schemas",
                                List.of(transactionSchema(4, 1036, "4"),
                                        transactionSchema(3, 1026, "3"),
                                        transactionSchema(2, 1016, "2"),
                                        transactionSchema(1, 1006, ""))),
                        named("transaction schema 4", transactionSchema(4, 1036, "4"))));
    }

    static Stream<Arguments> notFoundTestSchemas() {
        return Stream.of(
                arguments(
                        named("subject not found", userSchema(null, null, "")),
                        named("user schemas", List.of())),
                arguments(
                        named("no matching schema definition", addressSchema(null, null, "")),
                        named("address schemas", List.of(addressSchema(1, 1002, "updated")))),
                arguments(
                        named("no matching references",
                                updateReferences(
                                        paymentSchema(null, null, "3"),
                                        List.of(new SchemaReference("com.example.User", "first-user-subject", 1),
                                                new SchemaReference("com.example.User", "other-user-subject", 1)))),
                        named("payment schemas",
                                List.of(paymentSchema(3, 1024, "3"),
                                        paymentSchema(2, 1014, "2"),
                                        paymentSchema(1, 1004, "")))));
    }

    private static Schema updateReferences(Schema schema, List<SchemaReference> updatedReferences) {
        var alteredSchema = schema.copy();

        alteredSchema.setReferences(updatedReferences);

        return alteredSchema;
    }
}
