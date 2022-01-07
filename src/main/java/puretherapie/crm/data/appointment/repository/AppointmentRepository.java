package puretherapie.crm.data.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.appointment.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    Appointment findByIdAppointment(Integer idAppointment);

}