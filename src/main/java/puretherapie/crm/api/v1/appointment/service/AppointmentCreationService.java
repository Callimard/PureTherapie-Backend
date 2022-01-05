package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
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
import static puretherapie.crm.data.notification.NotificationLevel.BOSS_SECRETARY_LEVEL;

@Slf4j
@AllArgsConstructor
@Service
public class AppointmentCreationService {

    // Constants.

    private static final String NOTIFICATION_APPOINTMENT_CREATION_TITLE = "New appointment for client %s";
    private static final String NOTIFICATION_APPOINTMENT_CREATION_TEXT = "New appointment at %s for the client %s with the technician %s";

    public static final int MAX_OVERLAP_AUTHORIZED = 10;

    // Variables.

    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationCreationService notificationCreationService;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean createAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime beginTime) {
        return createAppointment(idClient, idTechnician, idAestheticCare, day, beginTime, 0);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean createAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime beginTime,
                                     int overlapAuthorized) {
        try {
            if (unCorrectDayOrBeginTime(day, beginTime)) return false;

            overlapAuthorized = verifyOverLap(overlapAuthorized);
            Client client = verifyClient(idClient);
            Technician technician = verifyTechnician(idTechnician);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);
            TimeSlot timeSlot = verifyTimeSlot(technician, day, beginTime, aestheticCare.getTimeExecution(), overlapAuthorized);
            Appointment appointment = buildAppointment(client, technician, aestheticCare, timeSlot);
            saveAppointment(appointment);
            createNotification(client, technician, timeSlot);
            return true;
        } catch (Exception e) {
            log.debug("Fail to create appointment", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private boolean unCorrectDayOrBeginTime(LocalDate day, LocalTime beginTime) {
        if (day == null || beginTime == null) {
            log.debug("Day or beginTime is null");
            return true;
        }
        return false;
    }

    private int verifyOverLap(int overlapAuthorized) {
        if (overlapAuthorized > MAX_OVERLAP_AUTHORIZED) {
            log.debug("Argument overlapAuthorized ({}) greater than MAX_OVERLAP_AUTHORIZED ({}) -> update value", overlapAuthorized,
                      MAX_OVERLAP_AUTHORIZED);
            overlapAuthorized = MAX_OVERLAP_AUTHORIZED;
        }

        if (overlapAuthorized < 0)
            overlapAuthorized = 0;
        return overlapAuthorized;
    }

    private Client verifyClient(int idClient) {
        Client c = clientRepository.findByIdPerson(idClient);
        if (c == null)
            throw new IllegalArgumentException("Not find client for idClient %s".formatted(idClient));
        return c;
    }

    private Technician verifyTechnician(int idTechnician) {
        Technician t = technicianRepository.findByIdPerson(idTechnician);
        if (t == null)
            throw new IllegalArgumentException("Not find technician for idTechnician %s".formatted(idTechnician));
        return t;
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        if (ac == null)
            throw new IllegalArgumentException("Not find aesthetic care for idAestheticCare %s".formatted(idAestheticCare));
        return ac;
    }

    private TimeSlot verifyTimeSlot(Technician technician, LocalDate day, LocalTime beginTime, int timeExecution, int overlapAuthorized) {
        if (thereIsTimeSlotAtDayTime(technician, day, beginTime)) {
            log.debug("Time Slot for technician {} already take for day {} at time {}", technician.simplyIdentifier(), day, beginTime);
            throw new TimeSlotOverlapException(
                    "Time Slot for technician %s already take for day %s at time %s".formatted(technician.simplyIdentifier(), day,
                                                                                               beginTime));
        }

        if (thereIsOverlap(technician, day, beginTime, timeExecution, overlapAuthorized)) {
            log.debug("Time Slot over lap find for the technician {} for the day {} at time {}", technician.simplyIdentifier(), day, beginTime);
            throw new TimeSlotOverlapException(
                    "Time Slot over lap find for the technician %s for the day %s at time %s".formatted(technician.simplyIdentifier(), day,
                                                                                                        beginTime));
        }

        // No overlap found
        TimeSlot timeSlot = timeSlotRepository.save(buildTimeSlot(technician, day, beginTime, timeExecution));
        log.debug("Create time slot {}", timeSlot);
        return timeSlot;
    }

    private boolean thereIsTimeSlotAtDayTime(Technician technician, LocalDate day, LocalTime beginTime) {
        TimeSlot atDayTimeTS = timeSlotRepository.findByTechnicianAndDayAndBegin(technician, day, beginTime);
        return atDayTimeTS != null && !atDayTimeTS.isFree();
    }

    private boolean thereIsOverlap(Technician technician, LocalDate day, LocalTime beginTime, int timeExecution, int overlapAuthorized) {
        List<TimeSlot> dayTS = timeSlotRepository.findByTechnicianAndDay(technician, day);
        if (dayTS != null)
            for (TimeSlot timeSlot : dayTS)
                if (hasOverlap(timeSlot, beginTime, timeExecution, overlapAuthorized))
                    return true;

        return false;
    }

    private boolean hasOverlap(TimeSlot existingTS, LocalTime beginTime, int lockTime, int overlapAuthorized) {
        LocalTime existingBeginTime = existingTS.getBegin();
        int existingLockTime = existingTS.getTime();

        if (!existingTS.isFree()) {
            if (existingBeginTime.isBefore(beginTime)) {
                // Verify if the new time slot is not in the existing time slot
                return hasOverlap(existingBeginTime, existingLockTime, beginTime, overlapAuthorized);
            } else if (existingBeginTime.isAfter(beginTime)) {
                // Verify if the existing time slot is not in the new time slot
                return hasOverlap(beginTime, lockTime, existingBeginTime, overlapAuthorized);
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
     * @param beforeBeginTime   the LocalTime before
     * @param beforeLockTime    the number of minute lock by the before time slot
     * @param afterBeginTime    the LocalTime after
     * @param overlapAuthorized overlap max minute authorized
     *
     * @return true if the after time slot is in the before time slot.
     *
     * @throws IllegalArgumentException if beforeBeginTime is not before the afterBeginTime
     */
    private boolean hasOverlap(LocalTime beforeBeginTime, int beforeLockTime, LocalTime afterBeginTime, int overlapAuthorized) {
        if (!beforeBeginTime.isBefore(afterBeginTime))
            throw new IllegalArgumentException("Argument beforeBeginTime %s is not before afterBeginTime %s".formatted(beforeBeginTime,
                                                                                                                       afterBeginTime));

        long minuteDiff = ChronoUnit.MINUTES.between(beforeBeginTime, afterBeginTime);
        if (minuteDiff < beforeLockTime - overlapAuthorized) {
            log.debug(
                    "Overlap between before TS beginTime {} lockTime {} and after TS beginTime {}", beforeBeginTime, beforeLockTime, afterBeginTime);
            return true;
        }

        if (minuteDiff < beforeLockTime)
            log.info("Sur booking done with a overlap of {} minutes", (beforeLockTime - minuteDiff));

        return false;
    }

    private TimeSlot buildTimeSlot(Technician technician, LocalDate day, LocalTime beginTime, int timeExecution) {
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
        boolean success = notificationCreationService.createNotification(NOTIFICATION_APPOINTMENT_CREATION_TITLE.formatted(client.simplyIdentifier()),
                                                                         NOTIFICATION_APPOINTMENT_CREATION_TEXT.formatted(timeSlot.getBegin(),
                                                                                                                          client.simplyIdentifier(),
                                                                                                                          technician.simplyIdentifier()),
                                                                         BOSS_SECRETARY_LEVEL, false);
        if (!success)
            log.error("Fail to create appointment notification");
    }

    private static class TimeSlotOverlapException extends RuntimeException {
        public TimeSlotOverlapException(String message) {
            super(message);
        }
    }
}
