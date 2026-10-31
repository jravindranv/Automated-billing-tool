package com.billing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String invoiceNumber;

    private LocalDateTime invoiceDate;

    // Customer details are copied onto the invoice so history stays intact if the customer is edited/deleted.
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String customerState;
    private String customerGstin;

    /** true => IGST applies; false => CGST + SGST. */
    private boolean interState;

    @Column(precision = 14, scale = 2) private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal cgst = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal sgst = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal igst = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal roundOff = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal grandTotal = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal amountPaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("paidAt ASC")
    private List<Payment> payments = new ArrayList<>();

    public BigDecimal getTotalTax() {
        return cgst.add(sgst).add(igst);
    }

    public BigDecimal getBalance() {
        return grandTotal.subtract(amountPaid);
    }

    public void addItem(InvoiceItem item) {
        item.setInvoice(this);
        items.add(item);
    }

    public void addPayment(Payment payment) {
        payment.setInvoice(this);
        payments.add(payment);
    }
}
