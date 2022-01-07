package puretherapie.crm.data.agenda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.technician.Technician;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {

    List<TimeSlot> findByTechnicianAndDay(Technician technician, LocalDate day);

    List<TimeSlot> findByAppointmentOrderByBeginAsc(Appointment appointment);

}
