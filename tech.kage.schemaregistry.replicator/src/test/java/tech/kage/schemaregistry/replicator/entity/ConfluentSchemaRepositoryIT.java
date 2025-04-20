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

import static tech.kage.schemaregistry.replicator.entity.ConfluentSchemaRepository.SCHEMAS_TOPIC;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.addressSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.customerProfileSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.orderSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.paymentSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.transactionSchema;
import static tech.kage.schemaregistry.replicator.test.data.TestSchemas.userSchema;

import java.time.Duration;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.test.StepVerifier;
import tech.kage.schemaregistry.replicator.entity.ConfluentSchemaRepository.SchemaKey;
import tech.kage.schemaregistry.replicator.entity.ConfluentSchemaRepository.SchemaValue;

/**
 * Integration tests for {@link ConfluentSchemaRepository}.
 *
 * @author Dariusz Szpakowski
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ConfluentSchemaRepositoryIT {
    // UUT
    @Autowired
    ConfluentSchemaRepository kafkaSchemaRepository;

    @Autowired
    Converter<Schema, ProducerRecord<SchemaKey, SchemaValue>> schemaConverter;

    @Autowired
    ReceiverOptions<?, ?> kafkaReceiverOptions;

    @Autowired
    KafkaAdmin kafkaAdmin;

    @Container
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.1");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Configuration
    @Import({ ConfluentSchemaRepository.class, KafkaAutoConfiguration.class })
    static class TestConfig {
        @Bean
        KafkaSender<?, ?> kafkaSender(SenderOptions<?, ?> senderOptions) {
            return KafkaSender.create(senderOptions);
        }

        @Bean
        SenderOptions<?, ?> kafkaSenderOptions(KafkaProperties properties) {
            var props = properties.buildProducerProperties(null);

            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

            return SenderOptions.create(props);
        }

        @Bean
        ReceiverOptions<?, ?> kafkaReceiverOptions(KafkaProperties properties) {
            var props = properties.buildConsumerProperties(null);

            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
            props.put("spring.json.trusted.packages", "*");

            return ReceiverOptions.<Object, byte[]>create(props);
        }
    }

    @BeforeEach
    void setUp() {
        kafkaAdmin.createOrModifyTopics(TopicBuilder.name(SCHEMAS_TOPIC).build());
    }

    @Test
    void savesSchemasInKafka() {
        // Given
        var schemas = List.of(
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

        var expectedSchemasIterator = schemas.iterator();

        // When
        var savedSchemas = Flux.concat(schemas.stream().map(schema -> kafkaSchemaRepository.save(schema)).toList());

        // Then
        var retrievedSchemas = KafkaReceiver
                .create(kafkaReceiverOptions.assignment(List.of(new TopicPartition(SCHEMAS_TOPIC, 0))))
                .receive()
                .take(schemas.size())
                .timeout(Duration.ofSeconds(60));

        StepVerifier
                .create(savedSchemas.thenMany(retrievedSchemas))
                .thenConsumeWhile(message -> {
                    var key = message.key();
                    var value = message.value();

                    var expectedSchema = schemaConverter.convert(expectedSchemasIterator.next());

                    return key.equals(expectedSchema.key()) && value.equals(expectedSchema.value());
                })
                .as("retrieves stored schemas with the same data")
                .verifyComplete();
    }
}
