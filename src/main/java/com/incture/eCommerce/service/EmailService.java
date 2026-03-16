package com.incture.eCommerce.service;

import com.incture.eCommerce.dto.OrderItemDto;
import com.incture.eCommerce.dto.OrderResponse;
import com.incture.eCommerce.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOrderConfirmation(String toEmail, OrderResponse order) {
        log.info("Preparing order confirmation email for: {}", toEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - Order #" + order.getOrderId());


            StringBuilder body = new StringBuilder();
            body.append("Hello,\n\n");
            body.append("Thank you for your purchase! Your order has been placed successfully.\n\n");
            body.append("Order Details:\n");
            body.append("Order ID: ").append(order.getOrderId()).append("\n");
            body.append("Total Amount: ₹").append(order.getTotalAmount()).append("\n");
            body.append("Status: ").append(order.getOrderStatus()).append("\n\n");

            body.append("Items Ordered:\n");
            for (OrderItemDto item : order.getItems()) {
                body.append("- ")
                        .append(item.getProductName())
                        .append(" (Qty: ")
                        .append(item.getQuantity())
                        .append(")")
                        .append(" - ₹")
                        .append(item.getTotalPrice())
                        .append("\n");
            }


            message.setText(body.toString());

            javaMailSender.send(message);
            log.info("Order confirmation email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}. Error: {}", toEmail, e.getMessage());
        }
    }


}
