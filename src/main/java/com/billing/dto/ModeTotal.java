package com.billing.dto;

import lombok.Value;
import com.billing.model.PaymentMode;
import java.math.BigDecimal;

@Value
public class ModeTotal {
    PaymentMode mode;
    BigDecimal total;
}
