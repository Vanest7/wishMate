package com.gifts.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wish {

    private Integer id;
    private User user;
    private Event event;
    private String description;
    private String purchaseUrl;
    private BigDecimal estimatedPrice;
}