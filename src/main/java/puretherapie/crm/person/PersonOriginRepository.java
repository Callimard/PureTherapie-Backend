package puretherapie.crm.person;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonOriginRepository extends JpaRepository<PersonOrigin, Long> {

    PersonOrigin findByType(String type);

}