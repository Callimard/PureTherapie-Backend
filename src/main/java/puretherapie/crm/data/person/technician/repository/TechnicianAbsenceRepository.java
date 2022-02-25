package puretherapie.crm.data.person.technician.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.TechnicianAbsence;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TechnicianAbsenceRepository extends JpaRepository<TechnicianAbsence, Integer> {

    List<TechnicianAbsence> findByTechnicianAndDay(Technician technician, LocalDate day);

}