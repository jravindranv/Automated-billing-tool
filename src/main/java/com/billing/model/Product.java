package com.billing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String sku;

    /** HSN / SAC code printed on the invoice. */
    private String hsnCode;

    /** Selling price per unit, excluding GST. */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    /** GST rate in percent, e.g. 0, 5, 12, 18, 28. */
    @NotNull(message = "GST rate is required")
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(precision = 5, scale = 2)
    private BigDecimal gstRate = new BigDecimal("18.00");

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    private String unit = "pcs";
}
