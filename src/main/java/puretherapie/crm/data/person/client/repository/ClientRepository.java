package puretherapie.crm.data.person.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.client.Client;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByIdPerson(Integer idPerson);

    Client findByEmail(String email);

    List<Client> findByFirstNameAndLastName(String firstName, String lastName);
}