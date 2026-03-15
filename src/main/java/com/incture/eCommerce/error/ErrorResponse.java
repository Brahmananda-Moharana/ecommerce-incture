package com.incture.eCommerce.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ErrorResponse {
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;
}
