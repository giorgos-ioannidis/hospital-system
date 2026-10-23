package gr.ihu.hospital.domain.appointment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/** Πληρωμή ραντεβού (UC-16, FR-17): τρόπος, ποσό και καταχώριση συναλλαγής. */
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionRef;
    private LocalDateTime paidAt;

    protected Payment() {
    }

    public Payment(Appointment appointment, PaymentMethod method) {
        this.appointment = appointment;
        this.method = method;
        this.amount = appointment.getService().getPrice();
        this.status = PaymentStatus.PENDING;
        appointment.attach(this);
    }

    public void markPaid(String reference, LocalDateTime when) {
        if (status == PaymentStatus.PAID) {
            throw new IllegalStateException("Η πληρωμή έχει ήδη καταχωρηθεί.");
        }
        this.transactionRef = reference;
        this.paidAt = when;
        this.status = PaymentStatus.PAID;
    }

    public Long getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
