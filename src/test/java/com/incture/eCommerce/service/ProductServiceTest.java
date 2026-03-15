package com.incture.eCommerce.service;

import com.incture.eCommerce.dto.ProductRequest;
import com.incture.eCommerce.entity.Product;
import com.incture.eCommerce.repository.ProductRepository;
import com.incture.eCommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product mockProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        // Dummy data for our incoming request
        productRequest = new ProductRequest();
        productRequest.setName("Gaming Laptop");
        productRequest.setDescription("High-end gaming laptop");
        productRequest.setPrice(1500.0);
        productRequest.setStock(10);
        productRequest.setCategory("Electronics");
        productRequest.setRating(4.8);

        // Dummy data for what the database would return
        mockProduct = Product.builder()
                .id(1L)
                .name("Gaming Laptop")
                .description("High-end gaming laptop")
                .price(1500.0)
                .stock(10)
                .category("Electronics")
                .rating(4.8)
                .active(true) // Active by default
                .build();
    }

    // --- TEST 1: Create Product ---
    @Test
    void testCreateProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        Product createdProduct = productService.addProduct(productRequest);

        assertNotNull(createdProduct);
        assertEquals("Gaming Laptop", createdProduct.getName());
        assertTrue(createdProduct.isActive());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // --- TEST 2: Get All Products (With Category) ---
    @Test
    void testGetAllProducts_WithCategory() {
        Page<Product> productPage = new PageImpl<>(List.of(mockProduct));

        // Mock the repository to return our page when queried by category and active=true
        when(productRepository.findByCategoryAndActiveTrue(eq("Electronics"), any(Pageable.class)))
                .thenReturn(productPage);

        Page<Product> result = productService.getAllProducts(0, 10, "Electronics", "id");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Gaming Laptop", result.getContent().get(0).getName());
    }

    // --- TEST 3: Get All Products (Without Category) ---
    @Test
    void testGetAllProducts_WithoutCategory() {
        Page<Product> productPage = new PageImpl<>(List.of(mockProduct));

        // Mock the repository to return all active products when category is null or empty
        when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(productPage);

        Page<Product> result = productService.getAllProducts(0, 10, "", "id");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // --- TEST 4: Get Product By ID ---
    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        Product foundProduct = productService.getProductById(1L);

        assertNotNull(foundProduct);
        assertEquals(1L, foundProduct.getId());
    }

    // --- TEST 5: Update Product ---
    @Test
    void testUpdateProduct_Success() {
        // Change the request to simulate an update
        productRequest.setPrice(1400.0);
        productRequest.setStock(15);

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        Product updatedProduct = productService.updateProduct(1L, productRequest);

        // Assert that the fields were updated before saving
        assertEquals(1400.0, updatedProduct.getPrice());
        assertEquals(15, updatedProduct.getStock());
        verify(productRepository, times(1)).save(mockProduct);
    }

    // --- TEST 6: Soft Delete Product ---
    @Test
    void testDeleteProduct_SoftDeleteSuccess() {
        // We ensure the product starts as active
        assertTrue(mockProduct.isActive());

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        productService.deleteProduct(1L);

        // Assert that the 'active' flag was switched to false (Soft Delete)
        assertFalse(mockProduct.isActive());

        // Verify that we SAVED the product with the new flag, instead of calling productRepository.delete()
        verify(productRepository, times(1)).save(mockProduct);
        verify(productRepository, never()).delete(any(Product.class));
    }
}