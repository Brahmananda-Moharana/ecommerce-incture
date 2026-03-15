package com.incture.eCommerce.service;

import com.incture.eCommerce.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import com.incture.eCommerce.dto.ProductRequest;
import com.incture.eCommerce.entity.Product;
import com.incture.eCommerce.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Adds a new product to the system.
     * Converts ProductRequest DTO to Product entity and saves it in the database.
     */
    public Product addProduct(ProductRequest request){

        log.info("Creating new product with name: {}", request.getName());

        // Convert incoming DTO into Product entity
        Product product = mapToProduct(request);


        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());

        return savedProduct;
    }


    /**
     * Fetch paginated list of products.
     * Supports optional category filtering and sorting.
     *
     * @param page     page number (0-based index)
     * @param size     number of records per page
     * @param category optional category filter
     * @param sortBy   field used for sorting
     */
    public Page<Product> getAllProducts(int page, int size, String category, String sortBy) {

        log.info("Fetching products page={}, size={}, category={}, sortBy={}",
                page, size, category, sortBy);

        // Create pageable object with pagination and sorting configuration
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // If category is provided, filter products by category and active status
        if (category != null && !category.trim().isEmpty()) {

            log.debug("Applying category filter: {}", category);

            return productRepository.findByCategoryAndActiveTrue(category, pageable);
        }


        return productRepository.findByActiveTrue(pageable);
    }


    /**
     * Fetch a single product by its ID.
     * Throws ResourceNotFoundException if product does not exist.
     */
    public Product getProductById(Long id) {

        log.info("Fetching product with id: {}", id);

        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });
    }

    /**
     * Updates product details.
     * Only non-null and valid fields from request are updated.
     */
    public Product updateProduct(Long id, ProductRequest request) {

        log.info("Updating product with id: {}", id);

        Product existingProduct = getProductById(id);


        if(validate(request.getName())){
            existingProduct.setName(request.getName());
        }


        if(validate(request.getDescription())){
            existingProduct.setDescription(request.getDescription());
        }

        if(request.getPrice() != null){
            existingProduct.setPrice(request.getPrice());
        }

        if(request.getStock() != null){
            existingProduct.setStock(request.getStock());
        }

        if(validate(request.getCategory())){
            existingProduct.setCategory(request.getCategory());
        }

        if(validate(request.getImageURL())){
            existingProduct.setImageURL(request.getImageURL());
        }

        if(request.getRating() != null){
            existingProduct.setRating(request.getRating());
        }

        // Update active status (used for soft delete or enabling/disabling product)(default value is true)
        existingProduct.setActive(request.isActive());

        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Product updated successfully with id: {}", updatedProduct.getId());

        return updatedProduct;
    }

    /**
     * Soft deletes a product.
     * Instead of removing the record from database,
     * it marks the product as inactive.
     */
    public void deleteProduct(Long id) {

        log.warn("Deleting product with id: {}", id);


        Product product = getProductById(id);

        product.setActive(false);

        productRepository.save(product);

        log.info("Product soft deleted with id: {}", id);
    }

    /**
     * Utility method to convert ProductRequest DTO into Product entity.
     */
    private Product mapToProduct(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .imageURL(request.getImageURL())
                .rating(request.getRating())
                .active(request.isActive())
                .build();
    }

    /**
     * Utility method used to validate String fields.
     * Returns true if string is not null and not empty.
     */
    private boolean validate(String s){
        return (s != null && !s.trim().isEmpty());
    }


}
