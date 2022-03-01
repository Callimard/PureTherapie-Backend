package puretherapie.crm.data.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.appointment.ClientAbsence;
import puretherapie.crm.data.person.client.Client;

import java.util.List;

@Repository
public interface ClientAbsenceRepository extends JpaRepository<ClientAbsence, Integer> {

    List<ClientAbsence> findByClient(Client client);

}