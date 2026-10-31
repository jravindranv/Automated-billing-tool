package com.billing.controller;

import com.billing.model.Invoice;
import com.billing.model.PaymentMode;
import com.billing.repository.CustomerRepository;
import com.billing.repository.ProductRepository;
import com.billing.service.BillingException;
import com.billing.service.BillingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class BillingController {

    private final BillingService billingService;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;

    public BillingController(BillingService billingService, ProductRepository productRepo, CustomerRepository customerRepo) {
        this.billingService = billingService;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
    }

    @GetMapping("/billing/new")
    public String form(Model model) {
        model.addAttribute("products", productRepo.findByStockGreaterThanOrderByNameAsc(0));
        model.addAttribute("customers", customerRepo.findAllByOrderByNameAsc());
        model.addAttribute("modes", PaymentMode.values());
        return "billing/new";
    }

    @PostMapping("/billing")
    public String create(@RequestParam(required = false) Long customerId,
                         @RequestParam(name = "productId", required = false) List<Long> productIds,
                         @RequestParam(name = "quantity", required = false) List<Integer> quantities,
                         @RequestParam(required = false) PaymentMode paymentMode,
                         @RequestParam(required = false) BigDecimal amountPaid,
                         @RequestParam(required = false) String reference,
                         RedirectAttributes ra) {
        try {
            Invoice invoice = billingService.createInvoice(customerId, productIds, quantities, paymentMode, amountPaid, reference);
            return "redirect:/invoices/" + invoice.getId();
        } catch (BillingException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/billing/new";
        }
    }
}
