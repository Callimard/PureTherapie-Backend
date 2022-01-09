package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.ClientAbsence;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientAbsenceRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static puretherapie.crm.api.v1.appointment.service.ClientDelayService.isTooMuchLateFromNow;
import static puretherapie.crm.tool.TimeTool.today;

@Slf4j
@AllArgsConstructor
@Service
public class ClientAbsenceService {

    // Constants.

    /**
     * In minutes.
     */
    public static final int CLIENT_ABSENCE_CHECK_DELAY = 1;

    // Variables.

    private final AppointmentRepository appointmentRepository;
    private final ClientAbsenceRepository clientAbsenceRepository;

    // Schedule methods.

    @Async
    @Scheduled(initialDelay = CLIENT_ABSENCE_CHECK_DELAY, fixedRate = CLIENT_ABSENCE_CHECK_DELAY, timeUnit = TimeUnit.MINUTES)
    public void checkClientAbsence() {
        log.debug("Start checking absence at {} {}", today(), LocalTime.now());
        List<Appointment> todayAppointments = appointmentRepository.findByDay(today());
        if (todayAppointments != null) {
            for (Appointment appointment : todayAppointments) {
                if (!appointment.isCanceled() && appointment.getClientArrival() == null && isTooMuchLateFromNow(appointment.getTime())) {
                    saveClientAbsence(appointment);
                    cancelAppointment(appointment);
                    log.info("Create Client absence for client {} for the appointment {} {}", appointment.getClient().simplyIdentifier(),
                             appointment.getDay(), appointment.getTime());
                }
            }
        }
    }

    private void saveClientAbsence(Appointment appointment) {
        ClientAbsence clientAbsence = buildClientAbsence(appointment);
        clientAbsence = clientAbsenceRepository.save(clientAbsence);
        log.debug("Save ClientAbsence {}", clientAbsence);
    }

    private ClientAbsence buildClientAbsence(Appointment appointment) {
        return ClientAbsence.builder()
                .client(appointment.getClient())
                .appointment(appointment)
                .level(AbsenceLevel.LOW.ordinal())
                .build();
    }

    private void cancelAppointment(Appointment appointment) {
        appointment.setCanceled(true);
        appointment = appointmentRepository.save(appointment);
        log.debug("Update appointment (set it to canceled) -> {}", appointment);
    }

    // Enum.

    public enum AbsenceLevel {
        IGNORE, LOW, MEDIUM, HIGH
    }

}
