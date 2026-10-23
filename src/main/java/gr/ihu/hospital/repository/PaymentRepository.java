package gr.ihu.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.ihu.hospital.domain.appointment.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
