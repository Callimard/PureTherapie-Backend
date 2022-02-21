package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.ClientAbsence;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientAbsenceRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.technician.Technician;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static puretherapie.crm.api.v1.appointment.service.ClientDelayService.isTooMuchLateFromNow;
import static puretherapie.crm.data.notification.NotificationLevel.BOSS_LEVEL;
import static puretherapie.crm.tool.TimeTool.today;

@Slf4j
@AllArgsConstructor
@Service
public class ClientAbsenceService {

    // Constants.

    private static final String CREATE_ABSENCE_TITLE = "Absence";
    private static final String CREATE_ABSENCE_TEXT = "Le client %s a été absent pour son RDV du %s à %s avec le/la technicien(ne) %s";

    /**
     * In minutes.
     */
    public static final int CLIENT_ABSENCE_CHECK_DELAY = 1;

    // Variables.

    private final AppointmentRepository appointmentRepository;
    private final ClientAbsenceRepository clientAbsenceRepository;
    private final NotificationCreationService notificationCreationService;

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
                    notifyAbsenceCreate(appointment.getClient(), appointment.getDay(), appointment.getTime(), appointment.getTechnician());
                    log.info("Create Client absence for client {} for the appointment {} {}", appointment.getClient().simplyIdentifier(),
                             appointment.getDay(), appointment.getTime());
                }
            }
        }
    }

    private void saveClientAbsence(Appointment appointment) {
        ClientAbsence clientAbsence = buildClientAbsence(appointment);
        clientAbsence = clientAbsenceRepository.save(clientAbsence);
        log.info("Save ClientAbsence {}", clientAbsence);
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
        log.info("Update appointment (set it to canceled) -> {}", appointment);
    }

    private void notifyAbsenceCreate(Client client, LocalDate day, LocalTime time, Technician technician) {
        boolean success = notificationCreationService.createNotification(CREATE_ABSENCE_TITLE,
                                                                         CREATE_ABSENCE_TEXT.formatted(client.simplyIdentifier(), day, time,
                                                                                                       technician.simplyIdentifier()),
                                                                         BOSS_LEVEL, true);
        if (!success)
            log.error("Fail to create notification for the absence");
    }

    // Enum.

    public enum AbsenceLevel {
        IGNORE, LOW, MEDIUM, HIGH
    }

}
