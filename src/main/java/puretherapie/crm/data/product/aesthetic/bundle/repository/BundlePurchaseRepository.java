package puretherapie.crm.data.product.aesthetic.bundle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;

import java.util.List;

@Repository
public interface BundlePurchaseRepository extends JpaRepository<BundlePurchase, Integer> {

    List<BundlePurchase> findByClient(Client client);

}