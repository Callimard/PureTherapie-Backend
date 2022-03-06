package puretherapie.crm.data.person.client.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.client.Client;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByIdPerson(Integer idPerson);

    List<Client> findByFirstNameLikeOrLastNameLike(String firstName, String lastName, Pageable pageable);

    List<Client> findByEmailLike(String email, Pageable pageable);

    List<Client> findByPhoneLike(String phone, Pageable pageable);

    List<Client> findByFirstNameLikeAndLastNameLikeAndEmailLikeAndPhoneLike(String firstName, String lastName, String email, String phone,
                                                                            Pageable pageable);

    List<Client> findByFirstNameAndLastName(String firstName, String lastName);

    Client findByEmail(String email);

    Client findByPhone(String phone);

}