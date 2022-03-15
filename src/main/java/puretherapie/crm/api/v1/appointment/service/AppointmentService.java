package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class AppointmentService {

    // Constants.

    public static final String APPOINTMENT_NOT_FOUND_ERROR = "appointment_not_found_error";

    // Variables.

    private final AppointmentRepository appointmentRepository;

    // Methods.

    public boolean isFirstAppointment(int idAppointment) {
        return isFirstAppointment(verifyAppointment(idAppointment));
    }

    public boolean isFirstAppointment(Appointment appointment) {
        Client client = appointment.getClient();
        List<Appointment> clientAppointments = appointmentRepository.findByClientOrderByDayAsc(client);
        Appointment firstNotCanceled = null;
        for (Appointment app : clientAppointments) {
            if (!app.isCanceled()) {
                firstNotCanceled = app;
                break;
            }
        }
        return firstNotCanceled != null && firstNotCanceled.getIdAppointment() == appointment.getIdAppointment();
    }

    private Appointment verifyAppointment(int idAppointment) {
        Appointment appointment = appointmentRepository.findByIdAppointment(idAppointment);
        if (appointment == null)
            throw new AppointmentServiceException(APPOINTMENT_NOT_FOUND_ERROR);

        return appointment;
    }

    public static class AppointmentServiceException extends RuntimeException {
        public AppointmentServiceException(String message) {
            super(message);
        }
    }
}
