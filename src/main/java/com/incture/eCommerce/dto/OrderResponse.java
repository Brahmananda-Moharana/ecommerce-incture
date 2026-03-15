package com.incture.eCommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private Double totalAmount;
    private LocalDateTime orderDate;
    private String paymentStatus;
    private String orderStatus;
    private List<OrderItemDto> items;
}
