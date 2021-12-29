package puretherapie.crm.person.client.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByIdPerson(long idPerson);

    Client findByMail(String mail);

    List<Client> findByFirstNameAndLastName(String firstName, String lastName);
}