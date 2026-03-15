package com.incture.eCommerce.service;

import com.incture.eCommerce.dto.OrderItemDto;
import com.incture.eCommerce.dto.OrderResponse;
import com.incture.eCommerce.entity.*;
import com.incture.eCommerce.exception.ResourceNotFoundException;
import com.incture.eCommerce.exception.UnauthorizedAccessException;
import com.incture.eCommerce.repository.CartRepository;
import com.incture.eCommerce.repository.OrderRepository;
import com.incture.eCommerce.repository.UserRepository;
import com.incture.eCommerce.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;


    /**
     * Converts the user's cart into an order (Checkout process).
     * Validates stock availability, simulates payment, creates order items,
     * and clears the cart if payment succeeds.
     */
    @Transactional
    public OrderResponse checkOut(){

        Long userId = SecurityUtil.getAuthenticatedUser().getId();

        log.info("Checkout initiated for userId={}", userId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found during checkout. userId={}", userId);
            return new ResourceNotFoundException("User not found with id: " + userId);
        });

        Cart cart = getCartByUserId(userId);

        if(cart.getItems().isEmpty()){
            log.warn("Checkout attempted with empty cart. userId={}", userId);
            throw new RuntimeException("Cannot checkout an empty cart!");
        }

        // Validate each cart item before placing the order
        // Ensures product is active and stock is sufficient
        for (CartItem item : cart.getItems()) {
            boolean isOutOfStock = (item.getProduct().getStock() < item.getQuantity());
            boolean isActive = item.getProduct().isActive();
            if (isOutOfStock || !isActive) {
                log.warn("Product out of stock during checkout. productId={}, requestedQty={}",
                        item.getProduct().getId(), item.getQuantity());
                throw new ResourceNotFoundException("Product out of stock: " + item.getProduct().getName());
            }
        }

        String paymentStatus;
        String orderStatus = "PLACED";

        if(simulatePaymentFailure()){
            paymentStatus = "FAILED";
            orderStatus = "CANCELLED";
            log.warn("Payment failed during checkout. userId={}", userId);
        } else {
            paymentStatus = "SUCCESS";
        }


        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        Order newOrder = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .orderDate(LocalDateTime.now())
                .paymentStatus(paymentStatus)
                .orderStatus(orderStatus)
                .build();

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(item->{
                    if(!paymentStatus.equals("FAILED")){
                        Product product = item.getProduct();
                        product.setStock(product.getStock() - item.getQuantity());
                    }
                    return mapToOrderItem(item, newOrder);
                })
                .collect(Collectors.toList());

        newOrder.setItems(orderItems);

        Order order = orderRepository.save(newOrder);

        log.info("Order created successfully. orderId={}, userId={}",
                order.getId(), userId);

        if(!"FAILED".equals(paymentStatus)){
            cart.getItems().clear();
            cart.setTotalPrice(0.0);

            log.debug("Cart cleared after successful checkout. userId={}", userId);

            cartRepository.save(cart);

            log.info("Payment successful. userId={}", userId);
        }

        return mapToOrderResponse(order);
    }

    /**
     * Fetch all orders belonging to a specific user.
     */
    public List<OrderResponse> getOrders(Long userId){

        log.info("Fetching orders for userId={}", userId);

        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetch a single order by ID.
     * Only the order owner or an ADMIN can access the order.
     */
    public OrderResponse getOrderById(Long orderId){

        log.info("Fetching order by id={}", orderId);

        User authUser = SecurityUtil.getAuthenticatedUser();

        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.error("Order not found. orderId={}", orderId);
            return new ResourceNotFoundException("No order is found with id: " + orderId);
        });


        boolean isAdmin = "ADMIN".equals(authUser.getRole());
        boolean isOwner = authUser.getId().equals(order.getUser().getId());

        if(!(isAdmin || isOwner)){
            log.error("Unauthorized order access attempt. orderId={}, userId={}",
                    orderId, authUser.getId());

            throw new UnauthorizedAccessException("User is not authorized");
        }
        return mapToOrderResponse(order);

    }

    /**
     * Update order status (e.g., SHIPPED, DELIVERED).
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String orderStatus){

        log.info("Updating order status. orderId={}, newStatus={}",
                orderId, orderStatus);

        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.error("Order not found while updating status. orderId={}", orderId);
            return new RuntimeException("No order is found with id: " + orderId);
        });

        // Prevent updates if payment failed or order cancelled
        if(order.getPaymentStatus().equals("FAILED") || order.getOrderStatus().equals("CANCELLED")){

            log.warn("Invalid order status update attempt. orderId={}, status={}",
                    orderId, orderStatus);
            throw new RuntimeException("Order is failed, you can't do:" + orderStatus);
        }

        order.setOrderStatus(orderStatus);

        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully. orderId={}, status={}",
                orderId, orderStatus);


        return mapToOrderResponse(orderRepository.save(order));
    }

    /**
     * Convert Order entity to OrderResponse DTO.
     */
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item->

                     OrderItemDto.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getPrice())
                            .totalPrice(item.getPrice() * item.getQuantity())
                            .build()
                ).collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .items(itemDtos)
                .build();
    }

    /**
     * Convert CartItem to OrderItem entity.
     */
    private OrderItem mapToOrderItem(CartItem item, Order order) {
        return OrderItem.builder()
                .order(order)
                .product(item.getProduct())
                .quantity(item.getQuantity())
                .price(item.getProduct().getPrice())
                .build();
    }

    private Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found. userId={}", userId);
                    return new ResourceNotFoundException("Cart not found for user ID: " + userId);
                });
    }
    // Protected so our test class can spy on it!
    protected boolean simulatePaymentFailure() {
        return Math.random() <= 0.2;
    }
}
