package com.incture.eCommerce.service;



import com.incture.eCommerce.dto.UserLoginRequest;
import com.incture.eCommerce.dto.UserRegistrationRequest;
import com.incture.eCommerce.dto.UserUpdateRequest;
import com.incture.eCommerce.entity.Cart;
import com.incture.eCommerce.entity.User;
import com.incture.eCommerce.exception.DuplicateResourceFoundException;
import com.incture.eCommerce.exception.ResourceNotFoundException;
import com.incture.eCommerce.repository.CartRepository;
import com.incture.eCommerce.repository.UserRepository;
import com.incture.eCommerce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {


    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    /**
     * Registers a new user.
     * Steps:
     * 1. Check if email already exists.
     * 2. Create a new User entity.
     * 3. Save the user.
     * 4. If the role is CUSTOMER, create an empty cart for the user.
     */
    @Transactional
    public User registerUser(UserRegistrationRequest request) {

        log.info("Registering new user with email: {}", request.getEmail());

        // Check if the email is already registered in the system
        if (userRepository.existsByEmail(request.getEmail())) {

            log.error("Registration failed. Email already in use: {}", request.getEmail());

            throw new DuplicateResourceFoundException("Email is already in use!");
        }


        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "CUSTOMER") // Default role
                .active(true) // New users are active by default
                .build();


        User savedUser = userRepository.save(newUser);

        log.info("User registered successfully with id: {}", savedUser.getId());

        // If the user is a CUSTOMER, create an empty cart for them
        if ("CUSTOMER".equals(savedUser.getRole())) {
            Cart newCart = Cart.builder()
                    .user(savedUser)
                    .totalPrice(0.0)
                    .build();

            cartRepository.save(newCart);

            log.debug("Cart created for user id: {}", savedUser.getId());
        }

        return savedUser;
    }


    /**
     * Retrieves a user by ID.
     * Throws exception if:
     * - user does not exist
     * - user is inactive
     */
    public User getUserById(Long id){

        log.info("Fetching user with id: {}", id);


        User user = userRepository.findById(id).orElse(null);

        // Validate that the user exists and is active
        if(user == null || !user.isActive()) {
            log.error("User not found or inactive with id: {}", id);
            throw new ResourceNotFoundException("User is not found with  id: " + id);
        }

        return user;
    }


    /**
     * Authenticates a user using email and password.
     * If authentication succeeds, a JWT token is generated.
     */
    public String loginUser(UserLoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());


        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        // Validate user existence and active status
        if(user == null || !user.isActive()) {
            log.error("Login failed. User not found or inactive for email: {}", request.getEmail());
            throw new ResourceNotFoundException("User is not found with email id: " + request.getEmail());
        }


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        log.info("Authentication successful for email: {}", request.getEmail());


        return jwtUtil.generateToken(user);
    }


    /**
     * Updates user details.
     * Only provided (non-null / non-empty) fields will be updated.
     */
    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {

        log.info("Updating user with id: {}", id);


        User existingUser = getUserById(id);


        if (validate(request.getName())) {
            existingUser.setName(request.getName());
        }


        if (validate(request.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (validate(request.getEmail())) {

            if(userRepository.existsByEmail(request.getEmail())){
                log.error("Email update failed. Email already exists: {}", request.getEmail());
                throw new DuplicateResourceFoundException("Email already exists");
            }

            existingUser.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);

        log.info("User updated successfully with id: {}", updatedUser.getId());


        return updatedUser;
    }

    /**
     * Soft deletes a user and permanently delete their associated cart.
     * Cart must be removed first to maintain referential integrity.
     */
    public void deleteUser(Long id) {

        log.warn("Deleting user with id: {}", id);


        User user = getUserById(id);

        //Soft delete the user
        user.setActive(false);

        // Delete user's cart if present
        cartRepository.findByUserId(id).ifPresent(cart -> {
            cartRepository.delete(cart);
            log.debug("Cart deleted for user id: {}", id);
        });

        userRepository.save(user);

        log.info("User soft deleted successfully with id: {}", id);
    }

    /**
     * Utility method to validate string fields.
     * Returns true if string is not null and not empty.
     */
    private boolean validate(String s){
        return (s != null && !s.isEmpty());
    }
}
