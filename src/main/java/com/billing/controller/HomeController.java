package com.billing.controller;

import com.billing.config.ShopProperties;
import com.billing.repository.CustomerRepository;
import com.billing.repository.InvoiceRepository;
import com.billing.repository.ProductRepository;
import com.billing.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
public class HomeController {

    private final ReportService reports;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;
    private final InvoiceRepository invoiceRepo;
    private final ShopProperties shop;

    public HomeController(ReportService reports, ProductRepository productRepo,
                          CustomerRepository customerRepo, InvoiceRepository invoiceRepo, ShopProperties shop) {
        this.reports = reports;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
        this.invoiceRepo = invoiceRepo;
        this.shop = shop;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.from(today);
        model.addAttribute("today", reports.summarize(today, today));
        model.addAttribute("month", reports.summarize(ym.atDay(1), ym.atEndOfMonth()));
        model.addAttribute("productCount", productRepo.count());
        model.addAttribute("customerCount", customerRepo.count());
        model.addAttribute("lowStock", productRepo.findByStockLessThanEqualOrderByStockAsc(shop.getLowStockThreshold()));
        model.addAttribute("recent", invoiceRepo.findTop5ByOrderByInvoiceDateDesc());
        return "index";
    }
}
