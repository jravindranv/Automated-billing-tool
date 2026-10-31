package com.billing.repository;

import com.billing.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findAllByOrderByNameAsc();
    List<Customer> findByNameContainingIgnoreCaseOrPhoneContainingOrderByNameAsc(String name, String phone);
}
