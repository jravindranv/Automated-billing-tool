## Internship Project
**Project Owner:** Jishnu R
**CODTECH Intern ID:** CT-6372
**Project Title:** Automated Billing Tool

# Invoice & Billing Generator (Spring Boot + Thymeleaf)

A general-purpose billing system with GST, printable invoices, payments, reports and automatic inventory updates.

## Features
| # | Feature | Where |
|---|---------|-------|
| 1 | Product management (add/edit/delete, price, GST %, HSN, stock) | `/products` |
| 2 | Customer management (name, phone, email, address, state, GSTIN) | `/customers` |
| 3 | Automated billing (pick products, live qty/total calculation) | `/billing/new` |
| 4 | GST: CGST+SGST (same state) or IGST (other state) | `BillingService` |
| 5 | Printable A4 invoice (Print / Save as PDF from the browser) | `/invoices/{id}` |
| 6 | Payments: Cash, UPI, Card; part-payments and later settlement | invoice page |
| 7 | Sales history with search | `/invoices` |
| 8 | Reports: daily, monthly, product-wise | `/reports/...` |
| 9 | Stock is deducted automatically when a bill is generated | `BillingService` |

## Requirements
* Java 17 or newer
* Maven 3.8+ (Lombok is used; in IntelliJ/Eclipse install the Lombok plugin and enable annotation processing)

## Database
* **H2 (default)** – file database stored in `./data/billingdb`. Console: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:file:./data/billingdb`, user `sa`, empty password).
* **MySQL** – edit `src/main/resources/application-mysql.properties` (URL/user/password), then:
  ```bash
  java -jar target/billing-app-1.0.0.jar --spring.profiles.active=mysql
  ```
  The database `billing_db` is created automatically if the MySQL user has permission.



## GST logic
* Product prices are stored **excluding GST**.
* Per line: `taxable = price x qty`, `GST = taxable x rate / 100` (rounded to 2 decimals).
* Total GST is split 50/50 into CGST and SGST (or shown fully as IGST).
* The grand total is rounded to the nearest rupee and the round-off is shown on the invoice.

## Project layout
```
src/main/java/com/billing
  model/       JPA entities (Product, Customer, Invoice, InvoiceItem, Payment)
  repository/  Spring Data repositories
  service/     BillingService (GST, stock, payments), ReportService
  controller/  MVC controllers
  dto/         report row/summary classes
  config/      ShopProperties, DataInitializer (demo data)
src/main/resources
  templates/   Thymeleaf pages     static/  CSS + billing.js
```

