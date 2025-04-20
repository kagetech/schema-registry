-- schemas
INSERT INTO schemas.schemas (id, schema) VALUES
(1001, '{
  "type": "record",
  "name": "User",
  "namespace": "com.example",
  "fields": [
    {"name": "id", "type": "long"},
    {"name": "username", "type": "string"},
    {"name": "email", "type": ["null", "string"], "default": null}
  ]
}'),
(1002, '{
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
}'),
(1022, '{
  "type": "record",
  "name": "Address",
  "namespace": "com.example",
  "fields": [
    {"name": "street", "type": "string"},
    {"name": "city2", "type": "string"},
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
}'),
(1003, '{
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
}'),
(1023, '{
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
          {"name": "quantity2", "type": "int"},
          {"name": "unitPrice", "type": "float"}
        ]
      }
    }},
    {"name": "totalAmount", "type": "double"}
  ]
}'),
(1004, '{
  "type": "record",
  "name": "Payment",
  "namespace": "com.example",
  "fields": [
    {"name": "paymentId", "type": "string"},
    {"name": "amount", "type": "double"},
    {"name": "user", "type": "com.example.User"}
  ]
}'),
(1014, '{
  "type": "record",
  "name": "Payment",
  "namespace": "com.example",
  "fields": [
    {"name": "paymentId", "type": "string"},
    {"name": "amount2", "type": "double"},
    {"name": "user", "type": "com.example.User"}
  ]
}'),
(1024, '{
  "type": "record",
  "name": "Payment",
  "namespace": "com.example",
  "fields": [
    {"name": "paymentId", "type": "string"},
    {"name": "amount3", "type": "double"},
    {"name": "user", "type": "com.example.User"}
  ]
}'),
(1005, '{
  "type": "record",
  "name": "CustomerProfile",
  "namespace": "com.example",
  "fields": [
    {"name": "profileId", "type": "string"},
    {"name": "user", "type": "com.example.User"},
    {"name": "primaryAddress", "type": "com.example.Address"},
    {"name": "lastUpdated", "type": "long"}
  ]
}'),
(1006, '{
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
}'),
(1016, '{
  "type": "record",
  "name": "Transaction",
  "namespace": "com.example",
  "fields": [
    {"name": "transactionId2", "type": "string"},
    {"name": "user", "type": "com.example.User"},
    {"name": "order", "type": "com.example.Order"},
    {"name": "timestamp", "type": "long"},
    {"name": "status", "type": {
      "type": "enum",
      "name": "TransactionStatus",
      "symbols": ["PENDING", "COMPLETED", "FAILED"]
    }}
  ]
}'),
(1026, '{
  "type": "record",
  "name": "Transaction",
  "namespace": "com.example",
  "fields": [
    {"name": "transactionId3", "type": "string"},
    {"name": "user", "type": "com.example.User"},
    {"name": "order", "type": "com.example.Order"},
    {"name": "timestamp", "type": "long"},
    {"name": "status", "type": {
      "type": "enum",
      "name": "TransactionStatus",
      "symbols": ["PENDING", "COMPLETED", "FAILED"]
    }}
  ]
}'),
(1036, '{
  "type": "record",
  "name": "Transaction",
  "namespace": "com.example",
  "fields": [
    {"name": "transactionId4", "type": "string"},
    {"name": "user", "type": "com.example.User"},
    {"name": "order", "type": "com.example.Order"},
    {"name": "timestamp", "type": "long"},
    {"name": "status", "type": {
      "type": "enum",
      "name": "TransactionStatus",
      "symbols": ["PENDING", "COMPLETED", "FAILED"]
    }}
  ]
}');

-- subjects
INSERT INTO schemas.subjects (subject, version, schema_id) VALUES
('user-subject', 1, 1001),
('address-subject', 1, 1002),
('address-subject', 2, 1022),
('order-subject', 1, 1003),
('order-subject', 2, 1023),
('payment-subject', 1, 1004),
('payment-subject', 2, 1014),
('payment-subject', 3, 1024),
('customer-profile-subject', 1, 1005),
('transaction-subject', 1, 1006),
('transaction-subject', 2, 1016),
('transaction-subject', 3, 1026),
('transaction-subject', 4, 1036);

-- references
INSERT INTO schemas.references (schema_id, name, subject, version) VALUES
(1004, 'com.example.User', 'user-subject', 1),
(1014, 'com.example.User', 'user-subject', 1),
(1024, 'com.example.User', 'user-subject', 1),
(1005, 'com.example.Address', 'address-subject', 2),
(1005, 'com.example.User', 'user-subject', 1),
(1006, 'com.example.Order', 'order-subject', 2),
(1006, 'com.example.User', 'user-subject', 1),
(1016, 'com.example.Order', 'order-subject', 2),
(1016, 'com.example.User', 'user-subject', 1),
(1026, 'com.example.Order', 'order-subject', 2),
(1026, 'com.example.User', 'user-subject', 1),
(1036, 'com.example.Order', 'order-subject', 2),
(1036, 'com.example.User', 'user-subject', 1);
