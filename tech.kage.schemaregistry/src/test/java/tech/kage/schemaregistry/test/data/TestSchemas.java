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

package tech.kage.schemaregistry.test.data;

import java.util.Arrays;
import java.util.Collections;

import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;

/**
 * Sample {@link Schema}s for use in tests.
 * 
 * @author Dariusz Szpakowski
 */
public class TestSchemas {
    public static Schema userSchema(Integer version, Integer id, String schemaChange) {
        String userSchemaString = """
                {
                  "type": "record",
                  "name": "User",
                  "namespace": "com.example",
                  "fields": [
                    {"name": "id", "type": "long"},
                    {"name": "username", "type": "string"},
                    {"name": "email", "type": ["null", "string"], "default": null}
                  ]
                }""".replace("username", "username" + schemaChange);

        return new Schema(
                "user-subject",
                version,
                id,
                "AVRO",
                Collections.emptyList(),
                userSchemaString);
    }

    public static Schema addressSchema(Integer version, Integer id, String schemaChange) {
        String addressSchemaString = """
                {
                  "type": "record",
                  "name": "Address",
                  "namespace": "com.example",
                  "fields": [
                    {"name": "street", "type": "string"},
                    {"name": "city", "type": "string"},
                    {"name": "state", "type": "string"},
                    {"name": "zipCode", "type": "string"},
                    {"name": "coordinates", "type": {
                      "type": "record",
                      "name": "Coordinates",
                      "fields": [
                        {"name": "latitude", "type": "double"},
                        {"name": "longitude", "type": "double"}
                      ]
                    }}
                  ]
                }""".replace("city", "city" + schemaChange);

        return new Schema(
                "address-subject",
                version,
                id,
                "AVRO",
                Collections.emptyList(),
                addressSchemaString);
    }

    public static Schema orderSchema(Integer version, Integer id, String schemaChange) {
        String orderSchemaString = """
                {
                  "type": "record",
                  "name": "Order",
                  "namespace": "com.example",
                  "fields": [
                    {"name": "orderId", "type": "string"},
                    {"name": "customerId", "type": "long"},
                    {"name": "orderDate", "type": "string"},
                    {"name": "items", "type": {
                      "type": "array",
                      "items": {
                        "type": "record",
                        "name": "Item",
                        "fields": [
                          {"name": "productId", "type": "string"},
                          {"name": "quantity", "type": "int"},
                          {"name": "unitPrice", "type": "float"}
                        ]
                      }
                    }},
                    {"name": "totalAmount", "type": "double"}
                  ]
                }""".replace("quantity", "quantity" + schemaChange);

        return new Schema(
                "order-subject",
                version,
                id,
                "AVRO",
                Collections.emptyList(),
                orderSchemaString);
    }

    public static Schema paymentSchema(Integer version, Integer id, String schemaChange) {
        String paymentSchemaString = """
                {
                  "type": "record",
                  "name": "Payment",
                  "namespace": "com.example",
                  "fields": [
                    {"name": "paymentId", "type": "string"},
                    {"name": "amount", "type": "double"},
                    {"name": "user", "type": "com.example.User"}
                  ]
                }""".replace("amount", "amount" + schemaChange);

        return new Schema(
                "payment-subject",
                version,
                id,
                "AVRO",
                Collections.singletonList(
                        new SchemaReference("com.example.User", "user-subject", 1)),
                paymentSchemaString);
    }

    public static Schema customerProfileSchema(Integer version, Integer id, String schemaChange) {
        String customerProfileSchemaString = """
                {
                  "type": "record",
                  "name": "CustomerProfile",
                  "namespace": "com.example",
                  "fields": [
                    {"name": "profileId", "type": "string"},
                    {"name": "user", "type": "com.example.User"},
                    {"name": "primaryAddress", "type": "com.example.Address"},
                    {"name": "lastUpdated", "type": "long"}
                  ]
                }""".replace("profileId", "profileId" + schemaChange);

        return new Schema(
                "customer-profile-subject",
                version,
                id,
                "AVRO",
                Arrays.asList(
                        new SchemaReference("com.example.Address", "address-subject", 2),
                        new SchemaReference("com.example.User", "user-subject", 1)),
                customerProfileSchemaString);
    }

    public static Schema transactionSchema(Integer version, Integer id, String schemaChange) {
        String transactionSchemaString = """
                {
                  "type": "record",
                  "name": "Transaction",
                  "namespace": "com.example",
                  "fields": [
                    {"name": "transactionId", "type": "string"},
                    {"name": "user", "type": "com.example.User"},
                    {"name": "order", "type": "com.example.Order"},
                    {"name": "timestamp", "type": "long"},
                    {"name": "status", "type": {
                      "type": "enum",
                      "name": "TransactionStatus",
                      "symbols": ["PENDING", "COMPLETED", "FAILED"]
                    }}
                  ]
                }""".replace("transactionId", "transactionId" + schemaChange);

        return new Schema(
                "transaction-subject",
                version,
                id,
                "AVRO",
                Arrays.asList(
                        new SchemaReference("com.example.Order", "order-subject", 2),
                        new SchemaReference("com.example.User", "user-subject", 1)),
                transactionSchemaString);
    }
}
