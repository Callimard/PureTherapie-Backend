package puretherapie.crm.data.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.agenda.ExceptionalOpening;

import java.time.LocalDate;
import java.util.List;

public interface ExceptionalOpeningRepository extends JpaRepository<ExceptionalOpening, Integer> {

    List<ExceptionalOpening> findByDay(LocalDate day);

}