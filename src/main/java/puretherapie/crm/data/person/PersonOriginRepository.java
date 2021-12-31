package puretherapie.crm.data.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonOriginRepository extends JpaRepository<PersonOrigin, Long> {

    PersonOrigin findByIdPersonOrigin(Long idPersonOrigin);

    PersonOrigin findByType(String type);

    List<PersonOrigin> findAll();
}