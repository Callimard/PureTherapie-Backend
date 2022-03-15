package puretherapie.crm.data.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.appointment.Surbooking;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SurbookingRepository extends JpaRepository<Surbooking, Integer> {

    Surbooking findByIdSurbooking(int idSurbooking);

    List<Surbooking> findByDayAndCanceled(LocalDate day, boolean canceled);

    List<Surbooking> findByCanceledFalseAndDayGreaterThanEqualAndDayLessThan(LocalDate begin, LocalDate end);

}