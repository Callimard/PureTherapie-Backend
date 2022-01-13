package puretherapie.crm.data.product.aesthetic.care.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;

@Repository
public interface SessionPurchaseRepository extends JpaRepository<SessionPurchase, Integer> {
}