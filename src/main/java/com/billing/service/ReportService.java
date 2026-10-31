package com.billing.service;

import com.billing.dto.DayRow;
import com.billing.dto.ModeTotal;
import com.billing.dto.ProductSalesRow;
import com.billing.dto.SalesSummary;
import com.billing.model.Invoice;
import com.billing.repository.InvoiceItemRepository;
import com.billing.repository.InvoiceRepository;
import com.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final InvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;
    private final InvoiceItemRepository itemRepo;

    public ReportService(InvoiceRepository invoiceRepo, PaymentRepository paymentRepo, InvoiceItemRepository itemRepo) {
        this.invoiceRepo = invoiceRepo;
        this.paymentRepo = paymentRepo;
        this.itemRepo = itemRepo;
    }

    private static LocalDateTime start(LocalDate d) { return d.atStartOfDay(); }
    private static LocalDateTime end(LocalDate d) { return d.atTime(LocalTime.MAX); }

    public List<Invoice> invoicesBetween(LocalDate from, LocalDate to) {
        return invoiceRepo.findByInvoiceDateBetweenOrderByInvoiceDateAsc(start(from), end(to));
    }

    public SalesSummary summarize(List<Invoice> invoices) {
        BigDecimal taxable = BigDecimal.ZERO, tax = BigDecimal.ZERO, total = BigDecimal.ZERO;
        for (Invoice i : invoices) {
            taxable = taxable.add(i.getSubtotal());
            tax = tax.add(i.getTotalTax());
            total = total.add(i.getGrandTotal());
        }
        return new SalesSummary(invoices.size(), taxable, tax, total);
    }

    public SalesSummary summarize(LocalDate from, LocalDate to) {
        return summarize(invoicesBetween(from, to));
    }

    public List<ModeTotal> paymentsByMode(LocalDate from, LocalDate to) {
        return paymentRepo.totalsByMode(start(from), end(to));
    }

    /** One row per day of the month that had sales. */
    public List<DayRow> dailyBreakdown(YearMonth month) {
        List<Invoice> invoices = invoicesBetween(month.atDay(1), month.atEndOfMonth());
        Map<LocalDate, List<Invoice>> byDay = invoices.stream()
                .collect(Collectors.groupingBy(i -> i.getInvoiceDate().toLocalDate(), TreeMap::new, Collectors.toList()));
        List<DayRow> rows = new ArrayList<>();
        byDay.forEach((day, list) -> {
            SalesSummary s = summarize(list);
            rows.add(new DayRow(day, s.getInvoiceCount(), s.getTaxable(), s.getTax(), s.getTotal()));
        });
        return rows;
    }

    public List<ProductSalesRow> productSales(LocalDate from, LocalDate to) {
        return itemRepo.productSales(start(from), end(to));
    }
}
