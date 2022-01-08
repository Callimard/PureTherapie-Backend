package puretherapie.crm.data.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.appointment.ClientArrival;

@Repository
public interface ClientArrivalRepository extends JpaRepository<ClientArrival, Integer> {
}