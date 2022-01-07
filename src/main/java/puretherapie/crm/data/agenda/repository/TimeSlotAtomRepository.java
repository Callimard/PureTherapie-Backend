package puretherapie.crm.data.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.agenda.TimeSlotAtom;

import java.util.List;

public interface TimeSlotAtomRepository extends JpaRepository<TimeSlotAtom, Integer> {

    List<TimeSlotAtom> findAllByOrderByEffectiveDate();

}