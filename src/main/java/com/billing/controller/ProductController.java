package com.billing.controller;

import com.billing.model.Product;
import com.billing.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        boolean search = q != null && !q.isBlank();
        model.addAttribute("products", search
                ? repo.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrderByNameAsc(q, q)
                : repo.findAllByOrderByNameAsc());
        model.addAttribute("q", q);
        return "products/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return repo.findById(id).map(p -> {
            model.addAttribute("product", p);
            return "products/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Product not found.");
            return "redirect:/products";
        });
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("product") Product product, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "products/form";
        repo.save(product);
        ra.addFlashAttribute("message", "Product saved.");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        repo.deleteById(id);
        ra.addFlashAttribute("message", "Product deleted.");
        return "redirect:/products";
    }
}
