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

package tech.kage.schemaregistry.entity;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static tech.kage.schemaregistry.test.data.TestSchemas.addressSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.customerProfileSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.orderSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.paymentSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.transactionSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.userSchema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import reactor.test.StepVerifier;

/**
 * Integration tests for {@link PostgresSchemaRepository}.
 * 
 * @author Dariusz Szpakowski
 */
@DataR2dbcTest
@ActiveProfiles("test")
class PostgresSchemaRepositoryIT {
    @Autowired
    SchemaRepository schemaRepository;

    @Configuration
    @EnableAutoConfiguration
    @Import(PostgresSchemaRepository.class)
    static class TestConfig {
    }

    @BeforeEach
    void setUp(@Autowired DatabaseClient dbClient, @Value("classpath:/test-data/schemas/ddl.sql") Resource ddl)
            throws IOException {
        dbClient
                .sql(ddl.getContentAsString(StandardCharsets.UTF_8))
                .fetch()
                .rowsUpdated()
                .block();
    }

    @ParameterizedTest
    @MethodSource("testSchemasBySubject")
    void findsSchemasBySubjectOrderedByVersionDesc(
            String subject,
            List<Schema> expectedSchemas,
            @Autowired DatabaseClient dbClient,
            @Value("classpath:/test-data/schemas/data.sql") Resource prepareData) throws IOException {
        // Given
        dbClient
                .sql(prepareData.getContentAsString(StandardCharsets.UTF_8))
                .fetch()
                .rowsUpdated()
                .block();

        // When
        var foundSchemas = schemaRepository.findBySubjectAndVersionOrderedByVersionDesc(subject, null);

        // Then
        StepVerifier
                .create(foundSchemas)
                .expectNextSequence(expectedSchemas)
                .as("finds expected schemas")
                .verifyComplete();
    }

    @Test
    void returnsEmptyFluxWhenSchemaNotFoundBySubject() {
        // Given
        var invalidSubject = "invalid-subject";

        // When
        var foundSchemas = schemaRepository.findBySubjectAndVersionOrderedByVersionDesc(invalidSubject, null);

        // Then
        StepVerifier
                .create(foundSchemas)
                .as("returns empty flux when schema not found by subject")
                .expectComplete();
    }

    @ParameterizedTest
    @MethodSource("testSchemasBySubjectAndVersion")
    void findsSchemaBySubjectAndVersion(
            String subject,
            Integer version,
            Schema expectedSchema,
            @Autowired DatabaseClient dbClient,
            @Value("classpath:/test-data/schemas/data.sql") Resource prepareData) throws IOException {
        // Given
        dbClient
                .sql(prepareData.getContentAsString(StandardCharsets.UTF_8))
                .fetch()
                .rowsUpdated()
                .block();

        // When
        var foundSchemas = schemaRepository.findBySubjectAndVersionOrderedByVersionDesc(subject, version);

        // Then
        StepVerifier
                .create(foundSchemas)
                .expectNext(expectedSchema)
                .as("finds expected schema")
                .verifyComplete();
    }

    @Test
    void returnsEmptyFluxWhenSchemaNotFoundBySubjectAndVersion() {
        // Given
        var invalidSubject = "invalid-subject";
        var invalidVersion = 123;

        // When
        var foundSchemas = schemaRepository.findBySubjectAndVersionOrderedByVersionDesc(invalidSubject, invalidVersion);

        // Then
        StepVerifier
                .create(foundSchemas)
                .as("returns empty flux when schema not found by subject and version")
                .expectComplete();
    }

    static Stream<Arguments> testSchemasBySubject() {
        return Stream.of(
                arguments(
                        "user-subject",
                        named("user schemas", List.of(userSchema(1, 1001, "")))),
                arguments(
                        "address-subject",
                        named("address schemas", List.of(addressSchema(2, 1022, "2"), addressSchema(1, 1002, "")))),
                arguments(
                        "order-subject",
                        named("order schemas", List.of(orderSchema(2, 1023, "2"), orderSchema(1, 1003, "")))),
                arguments(
                        "payment-subject",
                        named("payment schemas",
                                List.of(paymentSchema(3, 1024, "3"),
                                        paymentSchema(2, 1014, "2"),
                                        paymentSchema(1, 1004, "")))),
                arguments(
                        "customer-profile-subject",
                        named("customer profile schemas", List.of(customerProfileSchema(1, 1005, "")))),
                arguments(
                        "transaction-subject",
                        named("transaction schemas",
                                List.of(transactionSchema(4, 1036, "4"),
                                        transactionSchema(3, 1026, "3"),
                                        transactionSchema(2, 1016, "2"),
                                        transactionSchema(1, 1006, "")))));
    }

    static Stream<Arguments> testSchemasBySubjectAndVersion() {
        return Stream.of(
                arguments("user-subject", 1, named("user schema", userSchema(1, 1001, ""))),
                arguments("address-subject", 2, named("address schema", addressSchema(2, 1022, "2"))),
                arguments("order-subject", 2, named("order schema", orderSchema(2, 1023, "2"))),
                arguments("payment-subject", 3, named("payment schema", paymentSchema(3, 1024, "3"))),
                arguments("customer-profile-subject", 1,
                        named("customer profile schema", customerProfileSchema(1, 1005, ""))),
                arguments("transaction-subject", 4, named("transaction schema", transactionSchema(4, 1036, "4"))));
    }
}
