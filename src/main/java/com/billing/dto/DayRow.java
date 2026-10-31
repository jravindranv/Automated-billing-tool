package com.billing.dto;

import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class DayRow {
    LocalDate date;
    int invoiceCount;
    BigDecimal taxable;
    BigDecimal tax;
    BigDecimal total;
}
