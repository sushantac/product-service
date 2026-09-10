package com.ecommerce.product.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);
    private static final String CREATED_TOPIC = "product.catalog.created";
    private static final String UPDATED_TOPIC = "product.catalog.updated";
    private static final String DELETED_TOPIC = "product.catalog.deleted";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCreated(ProductEvent event) {
        kafkaTemplate.send(CREATED_TOPIC, event.productId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish product.created event for productId={}", event.productId(), ex);
                    } else {
                        log.info("Published product.created event for productId={}", event.productId());
                    }
                });
    }

    public void publishUpdated(ProductEvent event) {
        kafkaTemplate.send(UPDATED_TOPIC, event.productId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish product.updated event for productId={}", event.productId(), ex);
                    } else {
                        log.info("Published product.updated event for productId={}", event.productId());
                    }
                });
    }

    public void publishDeleted(ProductEvent event) {
        kafkaTemplate.send(DELETED_TOPIC, event.productId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish product.deleted event for productId={}", event.productId(), ex);
                    } else {
                        log.info("Published product.deleted event for productId={}", event.productId());
                    }
                });
    }
}
