package com.billing.controller;

import com.billing.model.Customer;
import com.billing.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository repo;

    public CustomerController(CustomerRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        boolean search = q != null && !q.isBlank();
        model.addAttribute("customers", search
                ? repo.findByNameContainingIgnoreCaseOrPhoneContainingOrderByNameAsc(q, q)
                : repo.findAllByOrderByNameAsc());
        model.addAttribute("q", q);
        return "customers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return repo.findById(id).map(c -> {
            model.addAttribute("customer", c);
            return "customers/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Customer not found.");
            return "redirect:/customers";
        });
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("customer") Customer customer, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "customers/form";
        repo.save(customer);
        ra.addFlashAttribute("message", "Customer saved.");
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        repo.deleteById(id);
        ra.addFlashAttribute("message", "Customer deleted.");
        return "redirect:/customers";
    }
}
