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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest registrationRequest;
    private User mockUser;
    private User mockAdmin;

    @BeforeEach
    public void setUp(){
        registrationRequest = UserRegistrationRequest.builder()
                .name("Brahma")
                .email("brahma@gmail.com")
                .password("123")
                .role("CUSTOMER")
                .build();
        mockUser = User.builder()
                .id(1L)
                .name("Brahma")
                .email("brahma@gmail.com")
                .password("encodedPassword")
                .role("CUSTOMER")
                .active(true)
                .build();
        mockAdmin = User.builder()
                .id(1L)
                .name("Brahma")
                .email("brahma@gmail.com")
                .password("encodedPassword")
                .role("ADMIN")
                .active(true)
                .build();
    }

    // --- TEST 1:  Registration ---
    @Test
    void testRegisterUser_Success() {
        // Arrange (Set up the mock behavior)
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());

        // Act (Call the method we want to test)
        User savedUser = userService.registerUser(registrationRequest);

        // Assert (Verify the results are exactly what we expect)
        assertNotNull(savedUser);
        assertEquals("Brahma", savedUser.getName());
        assertEquals("brahma@gmail.com", savedUser.getEmail());

        // Verify that a cart was also created and saved for this CUSTOMER
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(registrationRequest.getEmail()))
                .thenReturn(true);

        assertThrows(DuplicateResourceFoundException.class,
                () -> userService.registerUser(registrationRequest));
    }
    @Test
    void testRegisterAdmin_NoCartCreated() {
        registrationRequest.setRole("ADMIN");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(mockAdmin);

        userService.registerUser(registrationRequest);

        verify(cartRepository, never()).save(any());
    }



    @Test
    void loginUser_shouldReturnJwtToken_whenCredentialsAreValid() {

        // Arrange
        UserLoginRequest request = UserLoginRequest.builder()
                .email("brahma@gmail.com")
                .password("123")
                .build();

        when(userRepository.findByEmail("brahma@gmail.com"))
                .thenReturn(Optional.of(mockUser));

        when(jwtUtil.generateToken(mockUser))
                .thenReturn("mock-jwt-token");

        // Act
        String token = userService.loginUser(request);

        // Assert
        assertNotNull(token);
        assertEquals("mock-jwt-token", token);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("brahma@gmail.com", "123")
        );

        verify(jwtUtil).generateToken(mockUser);
    }

    @Test
    void loginUser_shouldThrowException_whenUserNotFound() {

        // Arrange
        UserLoginRequest request = UserLoginRequest.builder()
                .email("unknown@gmail.com")
                .password("123")
                .build();

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.loginUser(request));

        verify(authenticationManager, never()).authenticate(any());
    }
    @Test
    void loginUser_shouldThrowException_whenUserInactive() {

        // Arrange
        mockUser.setActive(false);

        UserLoginRequest request = UserLoginRequest.builder()
                .email("brahma@gmail.com")
                .password("123")
                .build();

        when(userRepository.findByEmail("brahma@gmail.com"))
                .thenReturn(Optional.of(mockUser));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.loginUser(request));

        verify(authenticationManager, never()).authenticate(any());
    }


    @Test
    void updateUser_shouldUpdateName_whenValidNameProvided() {

        // Arrange
        Long userId = 1L;

        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("UpdatedName")
                .build();

        // Mock repository to return existing user
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        // Mock save behavior
        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);

        // Act
        User updatedUser = userService.updateUser(userId, request);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("UpdatedName", updatedUser.getName());

        // Verify save operation occurred
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_shouldUpdatePassword_whenPasswordProvided() {

        // Arrange
        Long userId = 1L;

        UserUpdateRequest request = UserUpdateRequest.builder()
                .password("newPassword")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        // Mock password encoding
        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);

        // Act
        User updatedUser = userService.updateUser(userId, request);

        // Assert
        assertEquals("encodedNewPassword", mockUser.getPassword());

        // Ensure password encoding was used
        verify(passwordEncoder).encode("newPassword");

        // Ensure user was saved
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_shouldUpdateEmail_whenEmailIsUnique() {

        // Arrange
        Long userId = 1L;

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("newmail@gmail.com")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        // Email does not exist in database
        when(userRepository.existsByEmail("newmail@gmail.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);

        // Act
        User updatedUser = userService.updateUser(userId, request);

        // Assert
        assertEquals("newmail@gmail.com", updatedUser.getEmail());

        // Verify duplicate email check happened
        verify(userRepository).existsByEmail("newmail@gmail.com");

        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_shouldThrowException_whenEmailAlreadyExists() {

        // Arrange
        Long userId = 1L;

        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("existing@gmail.com")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        // Simulate duplicate email
        when(userRepository.existsByEmail("existing@gmail.com"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceFoundException.class,
                () -> userService.updateUser(userId, request));

        // Ensure save never occurs
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {

        // Arrange
        Long userId = 1L;

        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("UpdatedName")
                .build();

        // Simulate missing user
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userId, request));
    }


    @Test
    void getUserById_shouldReturnUser_whenUserExistsAndActive() {

        // Arrange
        Long userId = 1L;

        // Mock repository to return an active user
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        // Verify returned user is not null
        assertNotNull(result);

        // Verify the returned user ID matches
        assertEquals(userId, result.getId());

        // Verify repository method was called once
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_shouldThrowException_whenUserDoesNotExist() {

        // Arrange
        Long userId = 1L;

        // Simulate user not present in database
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        // Expect ResourceNotFoundException when user does not exist
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId));

        // Verify repository method was called
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_shouldThrowException_whenUserIsInactive() {

        // Arrange
        Long userId = 1L;

        // Mark the mock user as inactive
        mockUser.setActive(false);

        // Mock repository response
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        // Act & Assert
        // Expect exception because user is inactive
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId));

        // Verify repository interaction
        verify(userRepository).findById(userId);
    }

    @Test
    void deleteUser_shouldDeactivateUserAndDeleteCart_whenCartExists() {

        // Arrange
        Long userId = 1L;

        Cart cart = Cart.builder()
                .id(1L)
                .user(mockUser)
                .totalPrice(0.0)
                .build();

        // Mock repository behavior
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        // Act
        userService.deleteUser(userId);

        // Assert

        // Verify user was marked inactive
        assertFalse(mockUser.isActive());

        // Verify cart deletion happened
        verify(cartRepository).delete(cart);

        // Verify user was saved (soft delete)
        verify(userRepository).save(mockUser);
    }
    @Test
    void deleteUser_shouldDeactivateUser_whenCartDoesNotExist() {

        // Arrange
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        // Act
        userService.deleteUser(userId);

        // Assert

        // User should be inactive
        assertFalse(mockUser.isActive());

        // Cart deletion should never occur
        verify(cartRepository, never()).delete(any());

        // User should be saved
        verify(userRepository).save(mockUser);
    }@Test
    void deleteUser_shouldThrowException_whenUserNotFound() {

        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(userId));

        verify(userRepository, never()).save(any());
    }


}
