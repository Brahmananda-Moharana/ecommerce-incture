package com.incture.eCommerce.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Long cartId;
    private Double cartTotal;
    private List<CartItemDto> items;
}
