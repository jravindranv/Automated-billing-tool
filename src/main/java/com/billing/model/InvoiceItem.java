package com.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    // Snapshot of product data at the time of sale (no FK, so products can be edited/deleted freely).
    private Long productId;
    private String productName;
    private String hsnCode;
    private String unit;

    private int quantity;

    @Column(precision = 12, scale = 2) private BigDecimal unitPrice;
    @Column(precision = 5, scale = 2)  private BigDecimal gstRate;
    @Column(precision = 14, scale = 2) private BigDecimal taxableValue;
    @Column(precision = 14, scale = 2) private BigDecimal taxAmount;
    @Column(precision = 14, scale = 2) private BigDecimal lineTotal;
}
