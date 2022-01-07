package puretherapie.crm.data.person.technician.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.person.technician.LaunchBreak;
import puretherapie.crm.data.person.technician.Technician;

import java.time.LocalDate;

public interface LaunchBreakRepository extends JpaRepository<LaunchBreak, Integer> {

    LaunchBreak findByTechnicianAndDay(Technician technician, LocalDate day);

}