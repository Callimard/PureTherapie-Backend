package puretherapie.crm.data.product.bill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.bill.MeansOfPayment;

import java.util.List;

@Repository
public interface MeansOfPaymentRepository extends JpaRepository<MeansOfPayment, Integer> {

    MeansOfPayment findByIdMeansOfPayment(int idMeansOfPayment);

    List<MeansOfPayment> findAll();

}