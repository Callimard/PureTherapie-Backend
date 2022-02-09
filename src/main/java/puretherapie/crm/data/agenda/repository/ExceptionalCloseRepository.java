package puretherapie.crm.data.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.agenda.ExceptionalClose;

import java.time.LocalDate;
import java.util.List;

public interface ExceptionalCloseRepository extends JpaRepository<ExceptionalClose, Integer> {

    ExceptionalClose findByDay(LocalDate day);

    List<ExceptionalClose> findAll();

}