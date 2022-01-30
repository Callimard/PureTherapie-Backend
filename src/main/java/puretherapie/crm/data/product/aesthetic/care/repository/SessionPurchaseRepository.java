package puretherapie.crm.data.product.aesthetic.care.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;

import java.util.List;

@Repository
public interface SessionPurchaseRepository extends JpaRepository<SessionPurchase, Integer> {

    SessionPurchase findByIdSessionPurchase(int idSessionPurchase);

    List<SessionPurchase> findByClientAndAestheticCare(Client client, AestheticCare aestheticCare);

    List<SessionPurchase> findByClient(Client client);

}