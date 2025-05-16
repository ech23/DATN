package com.example.ogani.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {
    private long id;
    private String name;
    private long price;
    private int quantity;
    private long subTotal;
    private Long productId;
    private String productImage;
}