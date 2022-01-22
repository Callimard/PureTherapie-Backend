package puretherapie.crm.data.person.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.client.Client;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByIdPerson(Integer idPerson);

    List<Client> findByFirstNameAndLastNameAndEmailAndPhone(String firstName, String lastName, String email, String phone);

    List<Client> findByFirstNameAndLastNameAndEmail(String firstName, String lastName, String email);

    List<Client> findByFirstNameAndLastNameAndPhone(String firstName, String lastName, String phone);

    List<Client> findByFirstNameAndEmailAndPhone(String firstName, String email, String phone);

    List<Client> findByFirstNameAndLastName(String firstName, String lastName);

    List<Client> findByFirstNameAndEmail(String firstName, String email);

    List<Client> findByFirstNameAndPhone(String firstName, String phone);

    List<Client> findByLastNameAndEmailAndPhone(String lastName, String email, String phone);

    List<Client> findByLastNameAndEmail(String lastName, String email);

    List<Client> findByLastNameAndPhone(String lastName, String phone);

    List<Client> findByEmailAndPhone(String email, String phone);

    List<Client> findByFirstName(String firstName);

    List<Client> findByLastName(String lastName);

    Client findByEmail(String email);

    Client findByPhone(String phone);

}