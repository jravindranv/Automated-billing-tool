package com.billing.repository;

import com.billing.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrderByNameAsc(String name, String sku);
    List<Product> findAllByOrderByNameAsc();
    List<Product> findByStockGreaterThanOrderByNameAsc(int stock);
    List<Product> findByStockLessThanEqualOrderByStockAsc(int stock);
}
