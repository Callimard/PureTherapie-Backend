package puretherapie.crm.person.user.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByIdPerson(long idPerson);

    User findByUsername(String username);

}