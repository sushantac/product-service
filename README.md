# Product Service

Product catalog and search for the e-commerce platform: product CRUD, multi-field search, category filtering, price range, and pagination.

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | Search (q, category, price range, sort, page) |
| GET | `/api/v1/products/{id}` | Detail |
| POST | `/api/v1/products` | Create |
| PUT | `/api/v1/products/{id}` | Update |
| DELETE | `/api/v1/products/{id}` | Delete |
| GET | `/api/v1/categories` | List categories |
| POST | `/api/v1/categories` | Create category |

## Events

Published: `product.catalog.created`, `product.catalog.updated`, `product.catalog.deleted`.

## Stack

Java 21, Spring Boot 3.4, Liquibase (schema `product`), PostgreSQL, Redis, Kafka.

## Development

```bash
./mvnw spring-boot:run
# http://localhost:8083
```

Full local orchestration lives in the `sdlc` repo (`make dev`).