package com.billing.dto;

import lombok.Value;
import java.math.BigDecimal;

@Value
public class ProductSalesRow {
    String productName;
    Long quantity;
    BigDecimal revenue;
}
