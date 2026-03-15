package com.incture.eCommerce.controller;

import com.incture.eCommerce.dto.OrderResponse;
import com.incture.eCommerce.entity.Order;
import com.incture.eCommerce.entity.User;
import com.incture.eCommerce.service.OrderService;
import com.incture.eCommerce.util.SecurityUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    /**
     * API to place an order from the current user's cart.
     * The checkout process typically includes:
     * - Validating cart items
     * - Reducing product stock
     * - Creating order and order items
     * - Clearing the cart
     *
     * @return created order details
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(){

        return new ResponseEntity<>(orderService.checkOut(),HttpStatus.CREATED);
    }

    /**
     * API to fetch all orders belonging to the currently authenticated user.
     *
     * @return list of orders placed by the logged-in user
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>>  getOrders(){
        User user = SecurityUtil.getAuthenticatedUser();
        return ResponseEntity.ok(orderService.getOrders(user.getId()));
    }

    /**
     * API to fetch a specific order by order ID.
     *
     * Access rule:
     * - Order owner can access
     * - Admin can access
     *
     * Authorization validation is expected to be handled in the service layer.
     *
     * @param orderId order identifier
     * @return order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse>  getOrderById(@PathVariable Long orderId){
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    /**
     * API to update the status of an order.
     *
     * Typically used by ADMIN to update order lifecycle stages
     * such as: CREATED → SHIPPED → DELIVERED → CANCELLED
     *
     * @param orderId order identifier
     * @param status  new order status
     * @return updated order details
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId,
                                                           @RequestParam @NotBlank(message = "Status is required") String status){
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }




}
