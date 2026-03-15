package com.incture.eCommerce.controller;

import com.incture.eCommerce.dto.ProductRequest;
import com.incture.eCommerce.entity.Product;
import com.incture.eCommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * API to create a new product.
     * Validates the request body before sending it to the service layer.
     *
     * @param request ProductRequest DTO containing product details
     * @return created Product with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody ProductRequest request){
        return new ResponseEntity<>(productService.addProduct(request), HttpStatus.CREATED);
    }

    /**
     * API to fetch all products with pagination, sorting and optional category filtering.
     *
     * @param page     page number (default = 0)
     * @param size     number of records per page (default = 10)
     * @param category optional category filter
     * @param sortBy   field used for sorting (default = id)
     *
     * @return paginated list of products
     */
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "id") String sortBy) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, category, sortBy));
    }

    /**
     * API to fetch a single product using product ID.
     *
     * @param id product ID
     * @return Product if found, otherwise exception will be thrown from service layer
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * API to update an existing product.
     * Validates incoming request before updating.
     *
     * @param id      ID of the product to update
     * @param request updated product data
     * @return updated product
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable() Long id,@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>("Product deleted successfully",HttpStatus.NO_CONTENT);
    }
}
