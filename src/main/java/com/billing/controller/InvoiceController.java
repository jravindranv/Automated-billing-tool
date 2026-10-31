package com.billing.controller;

import com.billing.model.PaymentMode;
import com.billing.repository.InvoiceRepository;
import com.billing.service.BillingException;
import com.billing.service.BillingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceRepository repo;
    private final BillingService billingService;

    public InvoiceController(InvoiceRepository repo, BillingService billingService) {
        this.repo = repo;
        this.billingService = billingService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        boolean search = q != null && !q.isBlank();
        model.addAttribute("invoices", search ? repo.search(q.trim()) : repo.findAllByOrderByInvoiceDateDesc());
        model.addAttribute("q", q);
        return "invoices/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return repo.findById(id).map(inv -> {
            model.addAttribute("invoice", inv);
            model.addAttribute("modes", PaymentMode.values());
            return "invoices/view";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Invoice not found.");
            return "redirect:/invoices";
        });
    }

    @PostMapping("/{id}/payments")
    public String addPayment(@PathVariable Long id,
                             @RequestParam PaymentMode mode,
                             @RequestParam BigDecimal amount,
                             @RequestParam(required = false) String reference,
                             RedirectAttributes ra) {
        try {
            billingService.addPayment(id, mode, amount, reference);
            ra.addFlashAttribute("message", "Payment recorded.");
        } catch (BillingException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/invoices/" + id;
    }
}
