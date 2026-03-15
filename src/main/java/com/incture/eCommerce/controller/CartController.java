package com.incture.eCommerce.controller;

import com.incture.eCommerce.dto.CartResponse;
import com.incture.eCommerce.entity.User;
import com.incture.eCommerce.service.CartService;
import com.incture.eCommerce.util.SecurityUtil;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    /**
     * API to add a product to the authenticated user's cart.
     *
     * If the product already exists in the cart, the quantity
     * may be increased depending on service implementation.
     *
     * @param productId product identifier
     * @param quantity number of items to add (default = 1)
     * @return updated cart details
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<CartResponse> addProductToCart(@PathVariable @NotNull  Long productId,
                                                         @RequestParam(defaultValue = "1") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity){
        User authUser = SecurityUtil.getAuthenticatedUser();

        return ResponseEntity.ok(cartService.addProductToCart(authUser.getId(), productId,quantity));
    }

    /**
     * API to update the quantity of a product already present in the cart.
     *
     * If quantity becomes 0, the service layer may remove the item
     *
     * @param productId product identifier
     * @param quantity updated quantity
     * @return updated cart details
     */
    @PutMapping("/update/{productId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable @NotNull Long productId,
            @RequestParam @NotNull @Min(value = 0, message = "Quantity must be at least 0") Integer quantity){

        User authUser = SecurityUtil.getAuthenticatedUser();
        return new ResponseEntity<>(cartService.updateCartItemQuantity(
                authUser.getId(),
                productId,
                quantity), HttpStatus.OK);
    }

    /**
     * API to remove a specific product from the cart.
     *
     * @param productId product identifier
     * @return updated cart after removal
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeCartItemFromCart(
            @PathVariable @NotNull Long productId){
        User authUser = SecurityUtil.getAuthenticatedUser();
        return new ResponseEntity<>(cartService.removeCartItemFromCart(
                authUser.getId(),
                productId), HttpStatus.OK);
    }

    /**
     * API to retrieve the current authenticated user's cart.
     *
     * @return complete cart including items and totals
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        User authUser = SecurityUtil.getAuthenticatedUser();

        return ResponseEntity.ok(cartService.getCart(authUser.getId()));
    }
}





