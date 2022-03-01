package puretherapie.crm.data.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.agenda.GlobalOpeningTime;

import java.util.List;

public interface GlobalOpeningTimeRepository extends JpaRepository<GlobalOpeningTime, Integer> {

    List<GlobalOpeningTime> findByDayNumber(Integer dayNumber);

    List<GlobalOpeningTime> findAll();

}