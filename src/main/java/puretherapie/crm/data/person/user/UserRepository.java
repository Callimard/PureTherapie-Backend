package puretherapie.crm.data.person.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByIdPerson(Integer idPerson);

    User findByUsername(String username);

}