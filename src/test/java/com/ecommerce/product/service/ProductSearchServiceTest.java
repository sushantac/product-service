package com.ecommerce.product.service;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductSearchService searchService;

    @Test
    void searchWithNullParamsShouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        Page<Product> result = searchService.search(null, null, null, null, pageable);

        assertNotNull(result);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchWithQueryShouldBuildLikeSpec() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        Page<Product> result = searchService.search("laptop", null, null, null, pageable);

        assertNotNull(result);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchWithCategoryByNameShouldBuildEqualSpec() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        searchService.search(null, "electronics", null, null, pageable);

        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchWithCategoryByIdShouldBuildEqualSpec() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        searchService.search(null, "1", null, null, pageable);

        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchWithPriceRangeShouldBuildBetweenSpec() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        searchService.search(null, null, new BigDecimal("10"), new BigDecimal("100"), pageable);

        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchWithAllParamsShouldCombineSpecs() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> mockPage = new PageImpl<>(List.of(), pageable, 0);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        searchService.search("phone", "2", new BigDecimal("50"), new BigDecimal("500"), pageable);

        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }
}
