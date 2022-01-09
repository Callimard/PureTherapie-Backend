package puretherapie.crm.data.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.client.Client;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    Appointment findByIdAppointment(Integer idAppointment);

    Appointment findByClientAndDay(Client client, LocalDate day);

    List<Appointment> findByDay(LocalDate day);
}