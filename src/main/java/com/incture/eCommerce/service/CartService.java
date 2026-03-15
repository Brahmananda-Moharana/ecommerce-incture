package com.incture.eCommerce.service;

import com.incture.eCommerce.dto.CartItemDto;
import com.incture.eCommerce.dto.CartResponse;
import com.incture.eCommerce.entity.Cart;
import com.incture.eCommerce.entity.CartItem;
import com.incture.eCommerce.entity.Product;
import com.incture.eCommerce.exception.ResourceNotFoundException;
import com.incture.eCommerce.repository.CartRepository;
import com.incture.eCommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    /**
     * Fetch the cart of a specific user.
     * Converts Cart entity to CartResponse DTO before returning.
     */
    public CartResponse getCart(Long userId) {

        log.info("Fetching cart for userId={}", userId);

        return mapToResponse(getCartByUserId(userId));
    }

    /**
     * Adds a product to the user's cart.
     * If the product already exists in the cart, its quantity is increased.
     * If quantity is not given then add only one product to cart
     */
    public CartResponse addProductToCart(Long userId, Long productId, Integer quantity){

        log.info("Adding product to cart: userId={}, productId={}, quantity={}",
                userId, productId, quantity);

        Cart cart = getCartByUserId(userId);

        Product product = getProductByProductId(productId);

        Optional<CartItem> existingItem = getCartItemByProductIdFromCart(productId, cart);

        if(existingItem.isPresent()){

            // If item exists, increase quantity
            Integer existingCartItemQuantity = existingItem.get().getQuantity();
            Integer totalCartItemQuantity = existingCartItemQuantity + quantity;

            // Validate stock availability
            if(product.getStock() < totalCartItemQuantity){
                log.warn("Product out of stock. productId={}, requestedQty={}, availableStock={}",
                        productId, totalCartItemQuantity, product.getStock());
                throw  new RuntimeException("Product Out of Stock");
            }
            existingItem.get().setQuantity(totalCartItemQuantity);

            log.debug("Updated existing cart item quantity. productId={}, newQuantity={}",
                    productId, totalCartItemQuantity);
        }
        else{
            //Through exception if product stock is less than ordered quantity
            if(product.getStock() < quantity){
                throw new RuntimeException("Product Out of Stock");
            }

            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();

            cart.getItems().add(newCartItem);

            log.debug("New product added to cart. productId={}", productId);
        }

        return updateCartItemsTotalPriceAndSave(cart);
    }

    /**
     * Updates the quantity of a specific product in the cart.
     * If quantity is <= 0, the item is removed from the cart.
     */
    public CartResponse updateCartItemQuantity(Long userId, Long productId, Integer quantity){

        log.info("Updating cart item quantity: userId={}, productId={}, quantity={}",
                userId, productId, quantity);


        Cart cart = getCartByUserId(userId);

        Product product = getProductByProductId(productId);


        Optional<CartItem> existingItem = getCartItemByProductIdFromCart(productId, cart);

        if(existingItem.isEmpty()){
            log.error("Product not found in cart. productId={}", productId);
            throw new RuntimeException("Product not found in cart!");
        }

        // Remove item if quantity <= 0
        if(quantity <= 0){
            cart.getItems().remove(existingItem.get());
            log.warn("Cart item removed because quantity <= 0. productId={}", productId);
        }
        else if(product.getStock() < quantity){
            log.warn("Stock insufficient while updating cart. productId={}, requestedQty={}, availableStock={}",
                    productId, quantity, product.getStock());
            throw  new RuntimeException("Product Out of Stock");
        }
        else{
            existingItem.get().setQuantity(quantity);
            log.debug("Cart item quantity updated. productId={}, newQuantity={}",
                    productId, quantity);
        }

        return updateCartItemsTotalPriceAndSave(cart);


    }

    /**
     * Removes a specific product from the cart.
     */
    public CartResponse removeCartItemFromCart(Long userId, Long productId){

        log.info("Removing product from cart: userId={}, productId={}", userId, productId);

        Cart cart = getCartByUserId(userId);

        Optional<CartItem> item = getCartItemByProductIdFromCart(productId, cart);

        if(item.isEmpty()){
            throw new ResourceNotFoundException("Product not found in cart");
        }

        cart.getItems().remove(item.get());

        return updateCartItemsTotalPriceAndSave(cart);
    }

    /**
     * Recalculates the total cart price based on all items in the cart
     * and saves the updated cart.
     */
    private CartResponse updateCartItemsTotalPriceAndSave(Cart cart) {

        Double totalPrice =  cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);

        log.debug("Cart total recalculated. cartId={}, totalPrice={}", cart.getId(), totalPrice);

        return mapToResponse(cartRepository.save(cart));
    }


    /**
     * Converts Cart entity to CartResponse DTO.
     */
    private CartResponse mapToResponse(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(item -> CartItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getProduct().getPrice())
                        .totalPrice(item.getProduct().getPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getId())
                .cartTotal(cart.getTotalPrice())
                .items(items)
                .build();
    }

    /**
     * Fetch cart using userId.
     * Throws exception if cart does not exist.
     */
    private Cart getCartByUserId(Long userId) {

        log.info("Fetching cart for userId={}", userId);

        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for userId={}", userId);
                    return new ResourceNotFoundException("Cart not found for user ID: " + userId);
                });
    }


    /**
     * Fetch product by productId.
     */
    private Product getProductByProductId(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found. productId={}", productId);
                    return new ResourceNotFoundException("Product is not found with id: " + productId);
                });

        if(!product.isActive()){
            log.error("Attempt to add inactive product to cart. productId={}", productId);
            throw new ResourceNotFoundException("Product is no longer available");
        }

        return product;
    }


    /**
     * Fetch product by productId.
     */
    private Optional<CartItem> getCartItemByProductIdFromCart(Long productId, Cart cart){
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }

}
