package puretherapie.crm.data.product.bill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.bill.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Payment findByIdPayment(int idPayment);
}