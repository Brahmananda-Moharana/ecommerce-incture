package com.incture.eCommerce.service;

import com.incture.eCommerce.dto.CartResponse;
import com.incture.eCommerce.entity.Cart;
import com.incture.eCommerce.entity.CartItem;
import com.incture.eCommerce.entity.Product;
import com.incture.eCommerce.entity.User;
import com.incture.eCommerce.repository.CartRepository;
import com.incture.eCommerce.repository.ProductRepository;
import com.incture.eCommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User mockUser;
    private Cart mockCart;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        // 1. Setup a dummy User
        mockUser = User.builder().id(1L).email("test@test.com").build();

        // 2. Setup a dummy Cart belonging to that User (starts empty)
        mockCart = Cart.builder()
                .id(1L)
                .user(mockUser)
                .totalPrice(0.0)
                .items(new ArrayList<>()) // Initialize the list so we don't get NullPointerExceptions!
                .build();

        // 3. Setup a dummy Active Product
        mockProduct = Product.builder()
                .id(100L)
                .name("Wireless Mouse")
                .price(25.0)
                .stock(50)
                .active(true)
                .build();
    }

    // --- TEST 1: Add a NEW Product to the Cart ---
    @Test
    void testAddProductToCart_NewItemSuccess() {
        // Arrange
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        // Act: Add 2 wireless mice to the cart
        CartResponse response = cartService.addProductToCart(1L, 100L, 2);

        // Assert
        assertNotNull(response);
        assertEquals(1, mockCart.getItems().size()); // Cart should now have 1 item type
        assertEquals(50.0, mockCart.getTotalPrice()); // 2 items * $25.00 = $50.00
        verify(cartRepository, times(1)).save(mockCart);
    }

    // --- TEST 2: Add an EXISTING Product to the Cart (Increase Quantity) ---
    @Test
    void testAddProductToCart_ExistingItemIncreaseQuantity() {
        // Arrange: Pre-fill the cart with 1 mouse
        CartItem existingItem = CartItem.builder().cart(mockCart).product(mockProduct).quantity(1).build();
        mockCart.getItems().add(existingItem);
        mockCart.setTotalPrice(25.0);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        // Act: Add 3 MORE mice to the cart
        CartResponse response = cartService.addProductToCart(1L, 100L, 3);

        // Assert
        assertNotNull(response);
        assertEquals(1, mockCart.getItems().size()); // Still only 1 item type in the list
        assertEquals(4, mockCart.getItems().get(0).getQuantity()); // 1 existing + 3 new = 4 total
        assertEquals(100.0, mockCart.getTotalPrice()); // 4 items * $25.00 = $100.00
    }

    // --- TEST 3: Prevent Adding Inactive (Soft-Deleted) Products ---
    @Test
    void testAddProductToCart_FailsIfProductInactive() {
        // Arrange: Make the product inactive
        mockProduct.setActive(false);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addProductToCart(1L, 100L, 1);
        });

        assertEquals("Product is no longer available", exception.getMessage());

        // Verify we never attempted to save a modified cart
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // --- TEST 4: Update Item Quantity (and remove if <= 0) ---

    @Test
    void testUpdateCartItemQuantity_SetToZeroRemovesItem() {
        // Arrange: Pre-fill the cart with 1 mouse
        CartItem existingItem = CartItem.builder().cart(mockCart).product(mockProduct).quantity(1).build();
        mockCart.getItems().add(existingItem);

        // Mock 1: Return the cart
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));

        // Mock 2: THE FIX! We must return the product because your new method checks for it!
        when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));

        // Mock 3: Return the saved cart
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        // Act: Update quantity to 0
        CartResponse response = cartService.updateCartItemQuantity(1L, 100L, 0);

        // Assert
        assertTrue(mockCart.getItems().isEmpty()); // The item should be completely removed!
        assertEquals(0.0, mockCart.getTotalPrice());
    }

    // --- TEST 5: Remove Product From Cart completely ---
    @Test
    void testRemoveProductFromCart_Success() {
        // Arrange: Pre-fill the cart with 5 mice
        CartItem existingItem = CartItem.builder().cart(mockCart).product(mockProduct).quantity(5).build();
        mockCart.getItems().add(existingItem);
        mockCart.setTotalPrice(125.0);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        // Act: Remove the product entirely
        CartResponse response = cartService.removeCartItemFromCart(1L, 100L);

        // Assert
        assertTrue(mockCart.getItems().isEmpty());
        assertEquals(0.0, mockCart.getTotalPrice()); // Math recalculates to $0.00
    }
}