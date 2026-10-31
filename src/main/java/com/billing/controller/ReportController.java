package com.billing.controller;

import com.billing.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping({"", "/daily"})
    public String daily(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Model model) {
        LocalDate d = date != null ? date : LocalDate.now();
        var invoices = reports.invoicesBetween(d, d);
        model.addAttribute("type", "daily");
        model.addAttribute("date", d);
        model.addAttribute("invoices", invoices);
        model.addAttribute("summary", reports.summarize(invoices));
        model.addAttribute("modeTotals", reports.paymentsByMode(d, d));
        return "reports/report";
    }

    @GetMapping("/monthly")
    public String monthly(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                          Model model) {
        YearMonth ym = month != null ? month : YearMonth.now();
        model.addAttribute("type", "monthly");
        model.addAttribute("month", ym);
        model.addAttribute("rows", reports.dailyBreakdown(ym));
        model.addAttribute("summary", reports.summarize(ym.atDay(1), ym.atEndOfMonth()));
        model.addAttribute("modeTotals", reports.paymentsByMode(ym.atDay(1), ym.atEndOfMonth()));
        return "reports/report";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                           Model model) {
        LocalDate t = to != null ? to : LocalDate.now();
        LocalDate f = from != null ? from : t.withDayOfMonth(1);
        model.addAttribute("type", "products");
        model.addAttribute("from", f);
        model.addAttribute("to", t);
        model.addAttribute("productRows", reports.productSales(f, t));
        return "reports/report";
    }
}
