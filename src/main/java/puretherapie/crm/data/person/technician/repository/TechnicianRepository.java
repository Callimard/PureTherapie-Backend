package puretherapie.crm.data.person.technician.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.technician.Technician;

import java.util.List;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Integer> {

    Technician findByIdPerson(Integer idPerson);

    Technician findByEmail(String email);

    List<Technician> findAll();

    List<Technician> findByActive(boolean active);
}
