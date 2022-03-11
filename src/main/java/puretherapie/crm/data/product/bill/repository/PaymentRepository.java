package puretherapie.crm.data.product.bill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.bill.MeansOfPayment;
import puretherapie.crm.data.product.bill.Payment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Payment findByIdPayment(int idPayment);

    List<Payment> findByMeansOfPaymentAndCanceledAndPaymentDateGreaterThanEqualAndPaymentDateLessThan(MeansOfPayment meansOfPayment,
                                                                                                      boolean canceled,
                                                                                                      LocalDateTime begin,
                                                                                                      LocalDateTime end);
}