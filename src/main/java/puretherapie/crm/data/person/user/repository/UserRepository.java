package puretherapie.crm.data.person.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.user.Role;
import puretherapie.crm.data.person.user.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByIdPerson(Integer idPerson);

    User findByUsername(String username);

    List<User> findByRoles(Role role);

}