package com.billing.repository;

import com.billing.dto.ProductSalesRow;
import com.billing.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    @Query("""
           select new com.billing.dto.ProductSalesRow(it.productName, sum(it.quantity), sum(it.lineTotal))
           from InvoiceItem it
           where it.invoice.invoiceDate between :from and :to
           group by it.productName
           order by sum(it.lineTotal) desc
           """)
    List<ProductSalesRow> productSales(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
