package com.example.ogani.service;

import java.io.IOException;
import java.util.List;

import com.example.ogani.entity.Order;
import com.example.ogani.entity.OrderStatus;
import com.example.ogani.model.request.CreateOrderRequest;
import com.example.ogani.model.response.OrderResponse;

public interface OrderService {
    
    Order placeOrder(CreateOrderRequest request);

    List<Order> getList();
    
    List<Order> getOrderByUser(String username);
    
    Order getOrderById(long id);
    
    OrderResponse getOrderDetailById(long id);
    
    List<Order> getOrdersByStatus(OrderStatus status);
    
    List<Order> getOrdersByStatusAndUser(OrderStatus status, String username);
    
    Order updateOrderPaymentStatus(long id, String status, String paymentMethod);

    Order updateOrderStatus(long id, OrderStatus status);

}
