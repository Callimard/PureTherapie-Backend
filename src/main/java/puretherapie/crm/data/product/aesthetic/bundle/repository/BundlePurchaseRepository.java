package puretherapie.crm.data.product.aesthetic.bundle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;

@Repository
public interface BundlePurchaseRepository extends JpaRepository<BundlePurchase, Integer> {
}