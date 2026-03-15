package com.incture.eCommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequest {
    @NotBlank(message = "Name can't be empty")
    private String name;

    @NotBlank(message = "Email can't be empty")
    @Email(message = "Must be a valid email format")
    private String email;

    @NotBlank(message = "Password can't be empty")
    private String password;
    // default value will be "CUSTOMER"
    private String role;
}
