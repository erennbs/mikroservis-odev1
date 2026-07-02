package com.etiya.paymentservice.repositories;

import com.etiya.paymentservice.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA access to the {@code payments} table.
 */
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}
