package com.billing.repository;

import com.billing.dto.ModeTotal;
import com.billing.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
           select new com.billing.dto.ModeTotal(p.mode, sum(p.amount))
           from Payment p
           where p.paidAt between :from and :to
           group by p.mode
           order by p.mode
           """)
    List<ModeTotal> totalsByMode(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
