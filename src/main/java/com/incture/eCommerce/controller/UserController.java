package com.incture.eCommerce.controller;

import com.incture.eCommerce.dto.UserLoginRequest;
import com.incture.eCommerce.dto.UserRegistrationRequest;
import com.incture.eCommerce.dto.UserResponse;
import com.incture.eCommerce.dto.UserUpdateRequest;
import com.incture.eCommerce.entity.User;
import com.incture.eCommerce.exception.UnauthorizedAccessException;
import com.incture.eCommerce.service.UserService;
import com.incture.eCommerce.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {

        User savedUser = userService.registerUser(request);


        return new ResponseEntity<>(mapToResponse(savedUser), HttpStatus.CREATED);
    }

    /**
     * API to authenticate a user and generate a token.
     *
     * @param request login credentials
     * @return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody UserLoginRequest request) {


        return ResponseEntity.ok(userService.loginUser(request));
    }

    /**
     * API to fetch a user by ID.
     *
     * Access Rules:
     * - User can access their own profile
     * - Admin can access any profile
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = SecurityUtil.getAuthenticatedUser();
        boolean isAuthorized = (user.getId().equals(id) || "ADMIN".equals(user.getRole()));
        if(!isAuthorized){
            log.warn("Unauthorized access attempt by user {} for user id {}", user.getId(), id);
           throw new UnauthorizedAccessException("Unauthorized access");
        }

        return ResponseEntity.ok(mapToResponse(userService.getUserById(id)));
    }

    /**
     * API to update user details.
     *
     * Access Rule:
     * - Only the user themselves can update their profile.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable @Positive Long id, @RequestBody UserUpdateRequest request) {
        User user = SecurityUtil.getAuthenticatedUser();
        boolean isAuthorized = (user.getId().equals(id));
        if(!isAuthorized){
            log.warn("Unauthorized update attempt by user {} for user id {}", user.getId(), id);
            throw new UnauthorizedAccessException("Unauthorized access");
        }
        User updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(mapToResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
