package com.billing.config;

import com.billing.model.Customer;
import com.billing.model.Product;
import com.billing.repository.CustomerRepository;
import com.billing.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Adds a few demo products and customers on first run (only when the product table is empty). */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository products;
    private final CustomerRepository customers;

    public DataInitializer(ProductRepository products, CustomerRepository customers) {
        this.products = products;
        this.customers = customers;
    }

    @Override
    public void run(String... args) {
        if (products.count() > 0) return;

        products.save(p("Basmati Rice 1kg", "RICE-1", "1006", "95.00", "5", 120, "kg"));
        products.save(p("Sunflower Oil 1L", "OIL-1", "1512", "140.00", "5", 60, "ltr"));
        products.save(p("Notebook A4", "NB-A4", "4820", "45.00", "12", 200, "pcs"));
        products.save(p("Ball Pen (Blue)", "PEN-B", "9608", "10.00", "18", 500, "pcs"));
        products.save(p("LED Bulb 9W", "LED-9", "8539", "120.00", "18", 80, "pcs"));
        products.save(p("Bluetooth Speaker", "BT-SPK", "8518", "1499.00", "18", 15, "pcs"));
        products.save(p("Steel Water Bottle", "BOT-1", "7323", "350.00", "12", 8, "pcs"));
        products.save(p("Packaged Water 1L", "WTR-1", "2201", "20.00", "18", 300, "pcs"));

        customers.save(c("Anita Menon", "9847012345", "anita@example.com", "Kochi, Kerala", "Kerala", null));
        customers.save(c("Ravi Traders", "9900112233", "ravi@example.com", "MG Road, Bengaluru", "Karnataka", "29ABCDE1234F1Z5"));
        customers.save(c("Sunita Sharma", "9811122233", "sunita@example.com", "Karol Bagh, New Delhi", "Delhi", null));
    }

    private static Product p(String name, String sku, String hsn, String price, String gst, int stock, String unit) {
        Product p = new Product();
        p.setName(name);
        p.setSku(sku);
        p.setHsnCode(hsn);
        p.setPrice(new BigDecimal(price));
        p.setGstRate(new BigDecimal(gst));
        p.setStock(stock);
        p.setUnit(unit);
        return p;
    }

    private static Customer c(String name, String phone, String email, String address, String state, String gstin) {
        Customer c = new Customer();
        c.setName(name);
        c.setPhone(phone);
        c.setEmail(email);
        c.setAddress(address);
        c.setState(state);
        c.setGstin(gstin);
        return c;
    }
}
