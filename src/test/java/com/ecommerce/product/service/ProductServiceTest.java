package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryDto;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.event.ProductEvent;
import com.ecommerce.product.event.ProductEventPublisher;
import com.ecommerce.product.exception.ApiException;
import com.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductEventPublisher eventPublisher;

    @Mock
    private ProductSearchService searchService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("Electronics", "Electronic devices");
        category.setId(1L);

        product = new Product("Laptop", "Gaming laptop", new BigDecimal("999.99"), 10);
        product.setId(1L);
        product.setCategories(new java.util.HashSet<>(Set.of(category)));
        product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void createShouldSaveAndPublishEvent() {
        ProductRequest request = new ProductRequest("Laptop", "Gaming laptop",
                new BigDecimal("999.99"), 10, List.of(1L));

        when(categoryService.findById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals("Laptop", response.name());
        assertEquals(new BigDecimal("999.99"), response.price());
        assertEquals(10, response.stockQuantity());

        verify(productRepository).save(any(Product.class));
        verify(eventPublisher).publishCreated(any(ProductEvent.class));
    }

    @Test
    void createShouldPublishEventWithIdempotencyEventId() {
        ProductRequest request = new ProductRequest("Laptop", "Gaming laptop",
                new BigDecimal("999.99"), 10, null);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        productService.create(request);

        ArgumentCaptor<ProductEvent> captor = ArgumentCaptor.forClass(ProductEvent.class);
        verify(eventPublisher).publishCreated(captor.capture());
        ProductEvent event = captor.getValue();
        assertNotNull(event.eventId());
        assertEquals(2L, event.productId());
    }

    @Test
    void createShouldThrowWhenPriceNegative() {
        ProductRequest request = new ProductRequest("Laptop", "Gaming laptop",
                new BigDecimal("-10"), 10, null);

        assertThrows(Exception.class, () -> productService.create(request));
    }

    @Test
    void updateShouldRefetchCategoriesAndPublishEvent() {
        ProductRequest request = new ProductRequest("Gaming Laptop", "Updated",
                new BigDecimal("1299.99"), 5, List.of(1L));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryService.findById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.update(1L, request);

        assertEquals("Gaming Laptop", response.name());
        assertEquals(new BigDecimal("1299.99"), response.price());
        verify(eventPublisher).publishUpdated(any(ProductEvent.class));
    }

    @Test
    void updateShouldThrowWhenNotFound() {
        ProductRequest request = new ProductRequest("Laptop", "desc",
                new BigDecimal("99"), 1, null);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> productService.update(99L, request));
    }

    @Test
    void deleteShouldRemoveCategoryAssociationsAndPublishEvent() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
        verify(eventPublisher).publishDeleted(any(ProductEvent.class));
        assertTrue(category.getProducts().isEmpty());
    }

    @Test
    void deleteShouldThrowWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> productService.delete(99L));
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> productService.getById(99L));
    }

    @Test
    void getByIdShouldReturnProductWithCategories() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryService.toDto(category)).thenReturn(new CategoryDto(1L, "Electronics", "Electronic devices"));

        ProductResponse response = productService.getById(1L);

        assertEquals(1L, response.id());
        assertEquals(1, response.categories().size());
        assertEquals("Electronics", response.categories().getFirst().name());
    }
}
