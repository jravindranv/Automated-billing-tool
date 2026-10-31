package com.billing.repository;

import com.billing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByOrderByInvoiceDateDesc();

    List<Invoice> findTop5ByOrderByInvoiceDateDesc();

    List<Invoice> findByInvoiceDateBetweenOrderByInvoiceDateAsc(LocalDateTime from, LocalDateTime to);

    @Query("""
           select i from Invoice i
           where lower(i.invoiceNumber) like lower(concat('%', :q, '%'))
              or lower(i.customerName) like lower(concat('%', :q, '%'))
              or i.customerPhone like concat('%', :q, '%')
           order by i.invoiceDate desc
           """)
    List<Invoice> search(@Param("q") String q);
}
