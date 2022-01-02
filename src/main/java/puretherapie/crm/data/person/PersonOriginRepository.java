package puretherapie.crm.data.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static puretherapie.crm.data.person.PersonOrigin.NONE_TYPE;

@Repository
public interface PersonOriginRepository extends JpaRepository<PersonOrigin, Long> {

    // Methods.

    PersonOrigin findByIdPersonOrigin(Long idPersonOrigin);

    PersonOrigin findByType(String type);

    List<PersonOrigin> findAll();

    default PersonOrigin getNonePersonOrigin() {
        return findByType(NONE_TYPE);
    }
}