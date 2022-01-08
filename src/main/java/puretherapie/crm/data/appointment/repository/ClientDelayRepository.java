package puretherapie.crm.data.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.appointment.ClientDelay;

public interface ClientDelayRepository extends JpaRepository<ClientDelay, Integer> {
}