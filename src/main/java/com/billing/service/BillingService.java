package com.billing.service;

import com.billing.config.ShopProperties;
import com.billing.model.*;
import com.billing.repository.CustomerRepository;
import com.billing.repository.InvoiceRepository;
import com.billing.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core billing logic: totals, GST split (CGST+SGST vs IGST), stock deduction and payments.
 * Prices are GST-exclusive; GST is added on top.
 */
@Service
public class BillingService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TWO = new BigDecimal("2");

    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;
    private final InvoiceRepository invoiceRepo;
    private final ShopProperties shop;

    public BillingService(ProductRepository productRepo, CustomerRepository customerRepo,
                          InvoiceRepository invoiceRepo, ShopProperties shop) {
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.invoiceRepo = invoiceRepo;
        this.shop = shop;
    }

    @Transactional
    public Invoice createInvoice(Long customerId, List<Long> productIds, List<Integer> quantities,
                                 PaymentMode mode, BigDecimal amountPaid, String reference) {

        if (productIds == null || productIds.isEmpty() || quantities == null || productIds.size() != quantities.size()) {
            throw new BillingException("Add at least one product to the bill.");
        }

        // Merge duplicate product rows
        Map<Long, Integer> wanted = new LinkedHashMap<>();
        for (int i = 0; i < productIds.size(); i++) {
            Long pid = productIds.get(i);
            Integer qty = quantities.get(i);
            if (pid == null) continue;
            if (qty == null || qty <= 0) throw new BillingException("Quantity must be at least 1.");
            wanted.merge(pid, qty, Integer::sum);
        }
        if (wanted.isEmpty()) throw new BillingException("Add at least one product to the bill.");

        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(LocalDateTime.now());

        // Customer snapshot + decide GST type
        boolean interState = false;
        if (customerId != null) {
            Customer c = customerRepo.findById(customerId)
                    .orElseThrow(() -> new BillingException("Customer not found."));
            invoice.setCustomerId(c.getId());
            invoice.setCustomerName(c.getName());
            invoice.setCustomerPhone(c.getPhone());
            invoice.setCustomerAddress(c.getAddress());
            invoice.setCustomerState(c.getState());
            invoice.setCustomerGstin(c.getGstin());
            interState = c.getState() != null && !c.getState().isBlank()
                    && !c.getState().trim().equalsIgnoreCase(shop.getState().trim());
        } else {
            invoice.setCustomerName("Walk-in Customer");
        }
        invoice.setInterState(interState);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> e : wanted.entrySet()) {
            Product p = productRepo.findById(e.getKey())
                    .orElseThrow(() -> new BillingException("Product not found (id " + e.getKey() + ")."));
            int qty = e.getValue();
            if (p.getStock() < qty) {
                throw new BillingException("Insufficient stock for " + p.getName() + " (available: " + p.getStock() + ").");
            }

            BigDecimal taxable = money(p.getPrice().multiply(BigDecimal.valueOf(qty)));
            BigDecimal tax = money(taxable.multiply(p.getGstRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP));

            InvoiceItem item = new InvoiceItem();
            item.setProductId(p.getId());
            item.setProductName(p.getName());
            item.setHsnCode(p.getHsnCode());
            item.setUnit(p.getUnit());
            item.setQuantity(qty);
            item.setUnitPrice(money(p.getPrice()));
            item.setGstRate(p.getGstRate());
            item.setTaxableValue(taxable);
            item.setTaxAmount(tax);
            item.setLineTotal(taxable.add(tax));
            invoice.addItem(item);

            // Inventory: reduce stock automatically
            p.setStock(p.getStock() - qty);

            subtotal = subtotal.add(taxable);
            totalTax = totalTax.add(tax);
        }

        // GST split
        if (interState) {
            invoice.setIgst(totalTax);
            invoice.setCgst(BigDecimal.ZERO.setScale(2));
            invoice.setSgst(BigDecimal.ZERO.setScale(2));
        } else {
            BigDecimal half = money(totalTax.divide(TWO, 4, RoundingMode.HALF_UP));
            invoice.setCgst(half);
            invoice.setSgst(totalTax.subtract(half));
            invoice.setIgst(BigDecimal.ZERO.setScale(2));
        }

        BigDecimal raw = subtotal.add(totalTax);
        BigDecimal rounded = raw.setScale(0, RoundingMode.HALF_UP).setScale(2);
        invoice.setSubtotal(subtotal);
        invoice.setRoundOff(rounded.subtract(raw));
        invoice.setGrandTotal(rounded);

        // Initial payment (optional)
        if (amountPaid != null && amountPaid.signum() > 0) {
            BigDecimal paid = amountPaid.min(rounded);
            Payment pay = new Payment();
            pay.setMode(mode == null ? PaymentMode.CASH : mode);
            pay.setAmount(money(paid));
            pay.setReference(reference);
            pay.setPaidAt(LocalDateTime.now());
            invoice.addPayment(pay);
        }
        refreshPaymentStatus(invoice);

        invoice = invoiceRepo.save(invoice);
        invoice.setInvoiceNumber(String.format("INV-%d-%05d", invoice.getInvoiceDate().getYear(), invoice.getId()));
        return invoice;
    }

    @Transactional
    public void addPayment(Long invoiceId, PaymentMode mode, BigDecimal amount, String reference) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found."));
        if (amount == null || amount.signum() <= 0) throw new BillingException("Enter a valid amount.");
        if (amount.compareTo(invoice.getBalance()) > 0) {
            throw new BillingException("Amount exceeds the balance due (" + invoice.getBalance() + ").");
        }
        Payment pay = new Payment();
        pay.setMode(mode);
        pay.setAmount(money(amount));
        pay.setReference(reference);
        pay.setPaidAt(LocalDateTime.now());
        invoice.addPayment(pay);
        refreshPaymentStatus(invoice);
    }

    private void refreshPaymentStatus(Invoice invoice) {
        BigDecimal paid = invoice.getPayments().stream()
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setAmountPaid(money(paid));
        if (paid.signum() <= 0) invoice.setPaymentStatus(PaymentStatus.UNPAID);
        else if (paid.compareTo(invoice.getGrandTotal()) >= 0) invoice.setPaymentStatus(PaymentStatus.PAID);
        else invoice.setPaymentStatus(PaymentStatus.PARTIAL);
    }

    private static BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
