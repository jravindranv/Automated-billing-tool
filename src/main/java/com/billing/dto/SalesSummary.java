package com.billing.dto;

import lombok.Value;
import java.math.BigDecimal;

@Value
public class SalesSummary {
    int invoiceCount;
    BigDecimal taxable;
    BigDecimal tax;
    BigDecimal total;
}
