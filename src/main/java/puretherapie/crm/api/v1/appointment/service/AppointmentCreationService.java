package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.notification.service.NotificationService;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static puretherapie.crm.data.notification.NotificationLevel.BOSS_LEVEL;

@Slf4j
@AllArgsConstructor
@Service
public class AppointmentCreationService {

    // Constants.

    private static final String NOTIFICATION_APPOINTMENT_CREATION_TITLE = "New appointment for client %s";
    private static final String NOTIFICATION_APPOINTMENT_CREATION_TEXT = "New appointment at %s for the client %s with the technician %s";

    // Variables.

    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean createAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime timeBegin) {

        try {
            Client client = verifyClient(idClient);
            if (client == null)
                return false;

            Technician technician = verifyTechnician(idTechnician);
            if (technician == null)
                return false;

            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);
            if (aestheticCare == null)
                return false;

            TimeSlot timeSlot = verifyTimeSlot(technician, day, timeBegin, aestheticCare.getTimeExecution());
            if (timeSlot == null)
                return false;

            Appointment appointment = buildAppointment(client, technician, aestheticCare, timeSlot);
            if (appointment == null)
                return false;


            saveAppointment(appointment);

            createNotification(client, technician, timeSlot);

            return true;
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private Client verifyClient(int idClient) {
        return clientRepository.findByIdPerson(idClient);
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        return aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
    }

    private Technician verifyTechnician(int idTechnician) {
        return technicianRepository.findByIdPerson(idTechnician);
    }

    private TimeSlot verifyTimeSlot(Technician technician, LocalDate day, LocalTime beginTime, Integer timeExecution) {
        if (thereIsTimeSlotAtDayTime(technician, day, beginTime)) return null;

        if (thereIsOverlap(technician, day, beginTime, timeExecution)) return null;

        // No overlap found
        TimeSlot timeSlot = buildTimeSlot(technician, day, beginTime, timeExecution);

        return timeSlotRepository.save(timeSlot);
    }

    private boolean thereIsTimeSlotAtDayTime(Technician technician, LocalDate day, LocalTime beginTime) {
        TimeSlot atDayTimeTS = timeSlotRepository.findByTechnicianAndDayAndBegin(technician, day, beginTime);
        return atDayTimeTS != null && !atDayTimeTS.isFree();
    }

    private boolean thereIsOverlap(Technician technician, LocalDate day, LocalTime beginTime, Integer timeExecution) {
        List<TimeSlot> dayTS = timeSlotRepository.findByTechnicianAndDay(technician, day);
        if (dayTS != null)
            for (TimeSlot timeSlot : dayTS)
                if (hasOverlap(timeSlot, beginTime, timeExecution))
                    return true;

        return false;
    }

    private boolean hasOverlap(TimeSlot existingTS, LocalTime beginTime, Integer lockTime) {
        LocalTime existingBeginTime = existingTS.getBegin();
        int existingLockTime = existingTS.getTime();

        if (!existingTS.isFree()) {
            if (existingBeginTime.isBefore(beginTime)) {
                // Verify if the new time slot is not in the existing time slot
                return hasOverlap(existingBeginTime, existingLockTime, beginTime);
            } else if (existingBeginTime.isAfter(beginTime)) {
                // Verify if the existing time slot is not in the new time slot
                return hasOverlap(beginTime, lockTime, existingBeginTime);
            } else {
                // new time slot same has existing time slot
                log.error("New time slot same has existing time slot. Begin time new time slot = {}, begin time existing time slot {}", beginTime,
                          existingBeginTime);
                return true;
            }
        }
        return false;
    }

    /**
     * ORDER VERY IMPORTANT. The beforeBeginTime must be before the afterBeginTime. In other words, the method {@link LocalTime#isBefore(LocalTime)}
     * call on beforeBeginTime must return true. If it not the case, the throw an {@link IllegalArgumentException}
     *
     * @param beforeBeginTime the LocalTime before
     * @param beforeLockTime  the number of minute lock by the before time slot
     * @param afterBeginTime  the LocalTime after
     *
     * @return true if the after time slot is in the before time slot.
     *
     * @throws IllegalArgumentException if beforeBeginTime is not before the afterBeginTime
     */
    private boolean hasOverlap(LocalTime beforeBeginTime, Integer beforeLockTime, LocalTime afterBeginTime) {
        if (!beforeBeginTime.isBefore(afterBeginTime))
            throw new IllegalArgumentException("Argument beforeBeginTime %s is not before afterBeginTime %s".formatted(beforeBeginTime,
                                                                                                                       afterBeginTime));

        long minuteDiff = ChronoUnit.MINUTES.between(beforeBeginTime, afterBeginTime);
        if (minuteDiff < beforeLockTime) {
            log.debug(
                    "Overlap between before TS beginTime {} lockTime {} and after TS beginTime {}", beforeBeginTime, beforeLockTime, afterBeginTime);
            return true;
        }
        return false;
    }

    private TimeSlot buildTimeSlot(Technician technician, LocalDate day, LocalTime beginTime, Integer timeExecution) {
        return TimeSlot.builder()
                .day(day)
                .begin(beginTime)
                .time(timeExecution)
                .free(false)
                .technician(technician)
                .build();
    }

    private Appointment buildAppointment(Client client, Technician technician, AestheticCare aestheticCare, TimeSlot timeSlot) {
        return Appointment.builder()
                .client(client)
                .technician(technician)
                .aestheticCare(aestheticCare)
                .timeSlot(timeSlot)
                .canceled(false)
                .build();
    }

    private void saveAppointment(Appointment appointment) {
        Appointment a = appointmentRepository.save(appointment);
        log.info("Create appointment {}", a);
    }

    private void createNotification(Client client, Technician technician, TimeSlot timeSlot) {
        boolean success = notificationService.createNotification(NOTIFICATION_APPOINTMENT_CREATION_TITLE.formatted(client.simplyIdentifier()),
                                                                 NOTIFICATION_APPOINTMENT_CREATION_TEXT.formatted(timeSlot.getBegin(),
                                                                                                                  client.simplyIdentifier(),
                                                                                                                  technician.simplyIdentifier()),
                                                                 BOSS_LEVEL, false);
        if (!success)
            log.error("Fail to create appointment notification");
    }

}
