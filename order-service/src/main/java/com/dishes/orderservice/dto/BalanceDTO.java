package com.dishes.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BalanceDTO {
    private Long userId;
    private BigDecimal balance;
} 