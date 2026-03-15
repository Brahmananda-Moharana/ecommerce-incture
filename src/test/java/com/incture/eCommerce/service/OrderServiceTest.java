package com.incture.eCommerce.service;

import com.incture.eCommerce.dto.OrderResponse;
import com.incture.eCommerce.entity.*;
import com.incture.eCommerce.exception.ResourceNotFoundException;
import com.incture.eCommerce.exception.UnauthorizedAccessException;
import com.incture.eCommerce.repository.CartRepository;
import com.incture.eCommerce.repository.OrderRepository;
import com.incture.eCommerce.repository.UserRepository;
import com.incture.eCommerce.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    @InjectMocks
    private OrderService orderService;

    private User mockUser;
    private User mockAdmin;
    private Product mockProduct;
    private Cart mockCart;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).email("user@test.com").role("CUSTOMER").build();
        mockAdmin = User.builder().id(2L).email("admin@test.com").role("ADMIN").build();

        mockProduct = Product.builder()
                .id(100L)
                .name("Smartphone")
                .price(500.0)
                .stock(10)
                .active(true)
                .build();

        CartItem cartItem = CartItem.builder().id(1L).product(mockProduct).quantity(2).build();

        mockCart = Cart.builder()
                .id(1L)
                .user(mockUser)
                .totalPrice(1000.0)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();

        mockOrder = Order.builder()
                .id(500L)
                .user(mockUser)
                .totalAmount(1000.0)
                .orderDate(LocalDateTime.now())
                .paymentStatus("SUCCESS")
                .orderStatus("PLACED")
                .items(new ArrayList<>())
                .build();
    }

    // --- TEST 1: Successful Checkout ---
    @Test
    void testCheckOut_Success() {
        // We only mock SecurityUtil now, Math is safe!
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {

            // Arrange
            mockedSecurity.when(SecurityUtil::getAuthenticatedUser).thenReturn(mockUser);

            // THE FIX: Tell the spy to force the payment to succeed!
            doReturn(false).when(orderService).simulatePaymentFailure();

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
            when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

            // Act
            OrderResponse response = orderService.checkOut();

            // Assert
            assertNotNull(response);
            assertEquals("SUCCESS", response.getPaymentStatus());
            assertEquals(8, mockProduct.getStock());
            assertTrue(mockCart.getItems().isEmpty());
            assertEquals(0.0, mockCart.getTotalPrice());
            verify(cartRepository, times(1)).save(mockCart);
        }
    }

    // --- TEST 2: Checkout Fails Random Payment ---
    @Test
    void testCheckOut_PaymentFailed() {
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {

            // Arrange
            mockedSecurity.when(SecurityUtil::getAuthenticatedUser).thenReturn(mockUser);

            // THE FIX: Tell the spy to force the payment to FAIL!
            doReturn(true).when(orderService).simulatePaymentFailure();

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));

            mockOrder.setPaymentStatus("FAILED");
            mockOrder.setOrderStatus("CANCELLED");
            when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

            // Act
            OrderResponse response = orderService.checkOut();

            // Assert
            assertEquals("FAILED", response.getPaymentStatus());
            assertEquals(10, mockProduct.getStock()); // Stock not reduced
            assertFalse(mockCart.getItems().isEmpty()); // Cart not cleared
            verify(cartRepository, never()).save(mockCart);
        }
    }

    // --- TEST 3: Checkout with Out of Stock Product ---
    @Test
    void testCheckOut_OutOfStock() {
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtil::getAuthenticatedUser).thenReturn(mockUser);
            mockProduct.setStock(1); // Set stock lower than requested quantity (2)

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                orderService.checkOut();
            });

            assertTrue(exception.getMessage().contains("Product out of stock"));
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    // --- TEST 4: Get Order By Id (Owner Access) ---
    @Test
    void testGetOrderById_OwnerSuccess() {
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtil::getAuthenticatedUser).thenReturn(mockUser); // Owner is requesting
            when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder));

            // Act
            OrderResponse response = orderService.getOrderById(500L);

            // Assert
            assertNotNull(response);
            assertEquals(500L, response.getOrderId());
        }
    }

    // --- TEST 5: Get Order By Id (Unauthorized Access) ---
    @Test
    void testGetOrderById_Unauthorized() {
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            // Arrange
            User sneakyUser = User.builder().id(99L).role("CUSTOMER").build(); // Different user
            mockedSecurity.when(SecurityUtil::getAuthenticatedUser).thenReturn(sneakyUser);
            when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder)); // Order belongs to User 1

            // Act & Assert
            assertThrows(UnauthorizedAccessException.class, () -> {
                orderService.getOrderById(500L);
            });
        }
    }

    // --- TEST 6: Update Order Status Success ---
    @Test
    void testUpdateOrderStatus_Success() {
        // Arrange
        when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        OrderResponse response = orderService.updateOrderStatus(500L, "SHIPPED");

        // Assert
        assertEquals("SHIPPED", mockOrder.getOrderStatus());
        verify(orderRepository, times(2)).save(mockOrder); // Called twice in your method
    }

    // --- TEST 7: Update Order Status fails on Cancelled Order ---
    @Test
    void testUpdateOrderStatus_FailsOnCancelled() {
        // Arrange
        mockOrder.setPaymentStatus("FAILED");
        mockOrder.setOrderStatus("CANCELLED");
        when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus(500L, "SHIPPED");
        });

        assertTrue(exception.getMessage().contains("Order is failed"));
        verify(orderRepository, never()).save(any(Order.class));
    }
}