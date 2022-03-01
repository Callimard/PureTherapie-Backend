package puretherapie.crm.data.person.technician.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.person.technician.LaunchBreak;
import puretherapie.crm.data.person.technician.Technician;

import java.time.LocalDate;

@Repository
public interface LaunchBreakRepository extends JpaRepository<LaunchBreak, Integer> {

    LaunchBreak findByTechnicianAndDay(Technician technician, LocalDate day);

}