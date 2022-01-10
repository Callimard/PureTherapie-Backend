package puretherapie.crm.data.product.bill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.bill.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {
}