package puretherapie.crm.data.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.agenda.ExceptionalClose;

import java.time.LocalDate;

public interface ExceptionalCloseRepository extends JpaRepository<ExceptionalClose, Integer> {

    ExceptionalClose findByDay(LocalDate day);

}