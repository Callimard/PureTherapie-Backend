package puretherapie.crm.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonOriginRepository extends JpaRepository<PersonOrigin, Long> {

    PersonOrigin findByType(String type);

}