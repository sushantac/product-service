package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryRequest;
import com.ecommerce.product.dto.CategoryResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.exception.ApiException;
import com.ecommerce.product.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("Electronics", "Electronic devices");
        category.setId(1L);
    }

    @Test
    void createShouldSaveCategory() {
        CategoryRequest request = new CategoryRequest("Electronics", "Electronic devices");
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoryResponse response = categoryService.create(request);

        assertNotNull(response);
        assertEquals("Electronics", response.name());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createShouldThrowWhenDuplicateName() {
        CategoryRequest request = new CategoryRequest("Electronics", "desc");
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(category));

        assertThrows(ApiException.class, () -> categoryService.create(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void findAllShouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.findAll();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.getFirst().name());
    }

    @Test
    void findByIdShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.findById(1L);

        assertEquals("Electronics", result.getName());
    }

    @Test
    void findByIdShouldThrowWhenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> categoryService.findById(99L));
    }
}
