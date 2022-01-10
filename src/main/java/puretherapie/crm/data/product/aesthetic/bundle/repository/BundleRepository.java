package puretherapie.crm.data.product.aesthetic.bundle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.aesthetic.bundle.Bundle;

@Repository
public interface BundleRepository extends JpaRepository<Bundle, Integer> {

    Bundle findByIdBundle(int idBundle);

}