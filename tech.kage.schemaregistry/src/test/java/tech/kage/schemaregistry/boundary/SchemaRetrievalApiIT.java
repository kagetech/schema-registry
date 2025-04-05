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

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static tech.kage.schemaregistry.test.data.TestSchemas.addressSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.customerProfileSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.orderSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.paymentSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.transactionSchema;
import static tech.kage.schemaregistry.test.data.TestSchemas.userSchema;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import reactor.core.publisher.Mono;
import tech.kage.schemaregistry.control.SchemaRetrieval;

/**
 * Integration tests for the Schema Retrieval API, verifying endpoint behavior
 * for schema retrieval by subject.
 * 
 * @author Dariusz Szpakowski
 */
@WebFluxTest
@ActiveProfiles("test")
class SchemaRetrievalApiIT {
    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    SchemaRetrieval schemaRetrieval;

    @Configuration
    @Import(SubjectVersionResource.class)
    static class TestConfig {
    }

    @ParameterizedTest
    @MethodSource("testSchemasBySubject")
    void returns200AndLatestSchemaBySubject(String subject, Schema expectedSchema) {
        // Given
        given(schemaRetrieval.getLatestSchemaBySubject(subject)).willReturn(Mono.just(expectedSchema));

        var request = webTestClient.get().uri("/subjects/{subject}/versions/latest", subject);

        // When
        var response = request.exchange();

        // Then
        response
                .expectStatus().isOk()
                .expectBody(Schema.class).isEqualTo(expectedSchema);
    }

    @Test
    void returns404WhenNoLatestSchemaBySubjectFound() {
        // Given
        var invalidSubject = "invalid-subject";

        given(schemaRetrieval.getLatestSchemaBySubject(invalidSubject)).willReturn(Mono.empty());

        var request = webTestClient.get().uri("/subjects/{subject}/versions/latest", invalidSubject);

        // When
        var response = request.exchange();

        // Then
        response
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    @ParameterizedTest
    @MethodSource("testSchemasBySubjectAndVersion")
    void returns200AndSchemaFoundBySubjectAndVersion(String subject, Integer version, Schema expectedSchema) {
        // Given
        given(schemaRetrieval.getSchemaBySubjectAndVersion(subject, version))
                .willReturn(Mono.just(expectedSchema));

        var request = webTestClient.get().uri("/subjects/{subject}/versions/{version}", subject, version);

        // When
        var response = request.exchange();

        // Then
        response
                .expectStatus().isOk()
                .expectBody(Schema.class).isEqualTo(expectedSchema);
    }

    @Test
    void returns404WhenNoSchemaBySubjectAndVersionFound() {
        // Given
        var invalidSubject = "invalid-subject";
        var invalidVersion = 123;

        given(schemaRetrieval.getSchemaBySubjectAndVersion(invalidSubject, invalidVersion))
                .willReturn(Mono.empty());

        var request = webTestClient.get().uri("/subjects/{subject}/versions/{version}", invalidSubject, invalidVersion);

        // When
        var response = request.exchange();

        // Then
        response
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    static Stream<Arguments> testSchemasBySubject() {
        return Stream.of(
                arguments("user-subject", named("user schema", userSchema(1, 1001, ""))),
                arguments("address-subject", named("address schema", addressSchema(2, 1022, "2"))),
                arguments("order-subject", named("order schema", orderSchema(2, 1023, "2"))),
                arguments("payment-subject", named("payment schema", paymentSchema(3, 1024, "3"))),
                arguments("customer-profile-subject",
                        named("customer profile schema", customerProfileSchema(1, 1005, ""))),
                arguments("transaction-subject", named("transaction schema", transactionSchema(4, 1036, "4"))));
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
