package puretherapie.crm.data.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.appointment.ClientDelay;
import puretherapie.crm.data.person.client.Client;

import java.util.List;

public interface ClientDelayRepository extends JpaRepository<ClientDelay, Integer> {

    List<ClientDelay> findByClient(Client client);

}