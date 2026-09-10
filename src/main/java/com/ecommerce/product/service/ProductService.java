package com.ecommerce.product.service;

import com.ecommerce.product.dto.PaginatedResponse;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.event.ProductEvent;
import com.ecommerce.product.event.ProductEventPublisher;
import com.ecommerce.product.exception.ApiException;
import com.ecommerce.product.repository.ProductRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductEventPublisher eventPublisher;
    private final ProductSearchService searchService;

    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService,
                          ProductEventPublisher eventPublisher,
                          ProductSearchService searchService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.eventPublisher = eventPublisher;
        this.searchService = searchService;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product(request.name(), request.description(),
                request.price(), request.stockQuantity());

        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long catId : request.categoryIds()) {
                categories.add(categoryService.findById(catId));
            }
            product.setCategories(categories);
            for (Category cat : categories) {
                cat.getProducts().add(product);
            }
        }

        Product saved = productRepository.save(product);
        eventPublisher.publishCreated(
                ProductEvent.of(saved.getId(), saved.getName(), saved.getPrice(), saved.getStockQuantity()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product not found with id: " + id));
        return toResponse(product);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product not found with id: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());

        if (request.categoryIds() != null) {
            Set<Category> oldCategories = new HashSet<>(product.getCategories());
            for (Category oldCat : oldCategories) {
                oldCat.getProducts().remove(product);
            }
            product.getCategories().clear();

            Set<Category> newCategories = new HashSet<>();
            for (Long catId : request.categoryIds()) {
                Category cat = categoryService.findById(catId);
                cat.getProducts().add(product);
                newCategories.add(cat);
            }
            product.setCategories(newCategories);
        }

        Product saved = productRepository.save(product);
        eventPublisher.publishUpdated(
                ProductEvent.of(saved.getId(), saved.getName(), saved.getPrice(), saved.getStockQuantity()));
        return toResponse(saved);
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product not found with id: " + id));

        for (Category cat : product.getCategories()) {
            cat.getProducts().remove(product);
        }

        productRepository.delete(product);
        eventPublisher.publishDeleted(ProductEvent.deleted(id));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> search(String q, String category,
                                                      java.math.BigDecimal minPrice,
                                                      java.math.BigDecimal maxPrice,
                                                      String sort, int page, int size) {
        Pageable pageable = buildPageable(sort, page, size);
        Page<Product> result = searchService.search(q, category, minPrice, maxPrice, pageable);
        return toPaginated(result);
    }

    private Pageable buildPageable(String sort, int page, int size) {
        int pageSize = Math.min(size, 100);
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, pageSize);
        }
        return switch (sort) {
            case "price_asc" -> PageRequest.of(page, pageSize, Sort.by("price").ascending());
            case "price_desc" -> PageRequest.of(page, pageSize, Sort.by("price").descending());
            case "newest" -> PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
            default -> PageRequest.of(page, pageSize);
        };
    }

    private PaginatedResponse<ProductResponse> toPaginated(Page<Product> page) {
        java.util.List<ProductResponse> content = page.getContent().stream()
                .map(this::toResponse).toList();
        return new PaginatedResponse<>(
                content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    private ProductResponse toResponse(Product product) {
        var categories = product.getCategories().stream()
                .map(categoryService::toDto).toList();
        return new ProductResponse(
                product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStockQuantity(),
                categories, product.getCreatedAt());
    }
}
