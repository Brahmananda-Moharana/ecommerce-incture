package com.incture.eCommerce.dto;


import lombok.*;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
}
