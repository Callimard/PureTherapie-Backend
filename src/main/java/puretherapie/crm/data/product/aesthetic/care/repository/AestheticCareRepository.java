package puretherapie.crm.data.product.aesthetic.care.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;

@Repository
public interface AestheticCareRepository extends JpaRepository<AestheticCare, Integer> {

    AestheticCare findByIdAestheticCare(Integer idAestheticCare);

}
