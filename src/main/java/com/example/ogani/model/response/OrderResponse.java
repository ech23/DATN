package com.example.ogani.model.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ogani.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private long id;
    private String firstname;
    private String lastname;
    private String country;
    private String address;
    private String town;
    private String state;
    private long postCode;
    private String email;
    private String phone;
    private String note;
    private long totalPrice;
    private String paymentStatus;
    private OrderStatus orderStatus;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private String username;
    private List<OrderDetailResponse> orderDetails;
} 