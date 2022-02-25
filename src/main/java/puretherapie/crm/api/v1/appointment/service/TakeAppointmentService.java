package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.agenda.service.OpeningService;
import puretherapie.crm.api.v1.agenda.service.TimeSlotAtomService;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentFailDTO;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentResponseDTO;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentSuccessDTO;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.api.v1.person.technician.service.TechnicianAbsenceService;
import puretherapie.crm.api.v1.person.technician.service.TechnicianLaunchBreakService;
import puretherapie.crm.api.v1.person.technician.service.TechnicianService;
import puretherapie.crm.data.agenda.Opening;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.TimeSlotAtom;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.LaunchBreak;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.LaunchBreakRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static puretherapie.crm.data.agenda.Opening.correctTimeSlotTime;
import static puretherapie.crm.data.notification.NotificationLevel.BOSS_SECRETARY_LEVEL;
import static puretherapie.crm.tool.TimeTool.minuteBetween;

@Slf4j
@AllArgsConstructor
@Service
public class TakeAppointmentService {

    // Constants.

    private static final String NOTIFICATION_APPOINTMENT_CREATION_TITLE = "Nouveau RDV";
    private static final String NOTIFICATION_APPOINTMENT_CREATION_TEXT = "Nouveau RDV le %s à %s pour le client %s et le/la technicien(ne) %s";

    public static final String NULL_DAY_OR_BEGIN_TIME_ERROR = "null_day_begin_time";
    public static final String CLIENT_ID_NOT_FOUND_ERROR = "client_id_not_found";
    public static final String TECHNICIAN_ID_NOT_FOUND_ERROR = "technician_id_not_found";
    public static final String AESTHETIC_CARE_ID_NOT_FOUND_ERROR = "aesthetic_care_id_not_found";
    public static final String EXCEPTIONAL_CLOSE_ERROR = "exceptional_close";
    public static final String NOT_OPEN_ERROR = "not_open";
    public static final String NOT_IN_OPENING_TIME_ERROR = "not_in_opening_time";
    public static final String DURING_TECHNICIAN_ABSENCE_ERROR = "during_technician_absence_error";
    public static final String DURING_LAUNCH_BREAK_ERROR = "during_launch_break";
    public static final String INCOMPATIBLE_TIME_SLOT_TIME_ERROR = "incompatible_time_slot_time";
    public static final String OVERLAP_ERROR = "overlap_detect";

    // Variables.

    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final LaunchBreakRepository launchBreakRepository;
    private final TechnicianLaunchBreakService technicianLaunchBreakService;
    private final TechnicianService technicianService;
    private final TechnicianAbsenceService technicianAbsenceService;
    private final NotificationCreationService notificationCreationService;
    private final TimeSlotAtomService tsaService;
    private final OpeningService openingService;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TakeAppointmentResponseDTO takeAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime beginTime) {
        return takeAppointment(idClient, idTechnician, idAestheticCare, day, beginTime, false);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TakeAppointmentResponseDTO takeAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime beginTime,
                                                      boolean overlapAuthorized) {
        try {
            verifyDayOrBeginTime(day, beginTime);

            Client client = verifyClient(idClient);
            Technician technician = verifyTechnician(idTechnician);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);

            List<Opening> openingList = openingService.getOpenings(day);
            TimeSlotAtom tsa = tsaService.searchCorrectTSA(day);
            int nbTimeSlot = getNbTimeSlot(aestheticCare, tsa.getNumberOfMinutes(), overlapAuthorized);
            int appointmentDuration = computeAppointmentDuration(nbTimeSlot, tsa.getNumberOfMinutes());

            verifyExceptionalClose(day);
            verifyInstituteIsOpen(openingList);
            verifyInOpeningTime(beginTime, openingList, appointmentDuration);
            verifyTechnicianNotAbsent(technician, day, beginTime, appointmentDuration);
            verifyNotDuringLaunchBreak(technician, day, beginTime, appointmentDuration);
            verifyIsCompatibleTimeSlotTime(beginTime, openingList, tsa.getNumberOfMinutes());
            List<TimeSlot> allTimeSlots = generateAllTimeSlots(technician, day, beginTime, tsa.getNumberOfMinutes(), nbTimeSlot);
            verifyNoOverlapForAll(allTimeSlots);
            Appointment appointment = buildAppointment(client, technician, aestheticCare, day, beginTime);
            appointment = saveAppointment(appointment);
            saveAllTimeSlot(allTimeSlots, appointment);
            lockLaunchBreak(technician, day, tsa.getNumberOfMinutes());
            notifyAppointmentCreate(client, technician, day, beginTime);
            return takeAppointmentSuccess(client, technician, aestheticCare, day, beginTime);
        } catch (Exception e) {
            log.error("Fail to take appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return takeAppointmentFail(e.getMessage());
        }
    }

    private TakeAppointmentResponseDTO takeAppointmentSuccess(Client client, Technician technician, AestheticCare aestheticCare, LocalDate day,
                                                              LocalTime beginTime) {
        log.info("Taking appointment success for Client {}, Technician {}, Aesthetic Care {} at day {} at time {}", client.simplyIdentifier(),
                 technician.simplyIdentifier(), aestheticCare, day, beginTime);
        return TakeAppointmentSuccessDTO.builder()
                .success("Taking appointment success for Client %s, Technician %s, Aesthetic Care %s at day %s at time %s".formatted(
                        client.simplyIdentifier(), technician.simplyIdentifier(), aestheticCare, day, beginTime)).build();
    }

    private TakeAppointmentFailDTO takeAppointmentFail(String message) {
        return TakeAppointmentFailDTO.builder().error(message).build();
    }

    private void verifyExceptionalClose(LocalDate day) {
        if (openingService.hasExceptionClose(day)) {
            log.error("Exception close detect for the day {}", day);
            throw new TakeAppointmentException(EXCEPTIONAL_CLOSE_ERROR);
        }
    }

    private void saveAllTimeSlot(List<TimeSlot> timeSlots, Appointment appointment) {
        for (TimeSlot timeSlot : timeSlots) {
            timeSlot.setAppointment(appointment);
            TimeSlot ts = timeSlotRepository.save(timeSlot);
            log.debug("Save time slot {}", ts);
        }
    }

    private void verifyNoOverlapForAll(List<TimeSlot> timeSlots) {
        for (TimeSlot timeSlot : timeSlots)
            if (thereIsOverlap(timeSlot)) {
                log.error("Found overlap for time slot {}", timeSlot);
                throw new TakeAppointmentException(OVERLAP_ERROR);
            }
    }

    private List<TimeSlot> generateAllTimeSlots(Technician technician, LocalDate day, LocalTime beginTime, int tsNumberOfMinutes, int nbTimeSlot) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        LocalTime time = beginTime;
        for (int i = 0; i < nbTimeSlot; i++) {
            timeSlots.add(buildTimeSlotWithoutAppointment(technician, day, time, tsNumberOfMinutes));
            time = time.plusMinutes(tsNumberOfMinutes);
        }

        return timeSlots;
    }

    private void verifyIsCompatibleTimeSlotTime(LocalTime beginTime, List<Opening> openingList, int tsaNumberOfMinutes) {
        Set<LocalTime> correct = allCorrectTimeSlotTimes(openingList, tsaNumberOfMinutes);
        if (!correct.contains(beginTime)) {
            log.error("Incompatible time slot time for begin time {}", beginTime);
            throw new TakeAppointmentException(INCOMPATIBLE_TIME_SLOT_TIME_ERROR);
        }
    }

    private Set<LocalTime> allCorrectTimeSlotTimes(List<Opening> openingList, int tsaNumberOfMinutes) {
        Set<LocalTime> correctTimeSlotTimes = new HashSet<>();
        for (Opening opening : openingList) {
            List<LocalTime> correctTimeSlot = correctTimeSlotTime(opening, tsaNumberOfMinutes);
            correctTimeSlotTimes.addAll(correctTimeSlot);
        }
        return correctTimeSlotTimes;
    }

    private void verifyTechnicianNotAbsent(Technician technician, LocalDate day, LocalTime beginTime, int appointmentDuration) {
        if (technicianAbsenceService.isInTechnicianAbsence(technician, day, beginTime, appointmentDuration))
            throw new TakeAppointmentException(DURING_TECHNICIAN_ABSENCE_ERROR);
    }

    private void verifyNotDuringLaunchBreak(Technician technician, LocalDate appointmentDay, LocalTime appointmentBeginTime,
                                            int appointmentDuration) {
        if (technicianLaunchBreakService.isDuringTechnicianLaunchBreak(technician, appointmentDay, appointmentBeginTime, appointmentDuration))
            throw new TakeAppointmentException(DURING_LAUNCH_BREAK_ERROR);
    }

    private int computeAppointmentDuration(int nbTimeSlot, int tsNumberOfMinutes) {
        return nbTimeSlot * tsNumberOfMinutes;
    }

    private int getNbTimeSlot(AestheticCare aestheticCare, int tsNumberOfMinutes, boolean authorizedOverlap) {
        int acExecutionTime = aestheticCare.getTimeExecution();
        int nbTimeSlot = 1;
        int rest = 0;

        if (isMoreThanOneTimeSlot(tsNumberOfMinutes, acExecutionTime)) {
            nbTimeSlot = acExecutionTime / tsNumberOfMinutes;
            rest = acExecutionTime % tsNumberOfMinutes;
        }

        if (!authorizedOverlap && rest > 0)
            nbTimeSlot += 1;
        return nbTimeSlot;
    }

    private boolean isMoreThanOneTimeSlot(int timeSlotDuration, int acExecutionTime) {
        return acExecutionTime > timeSlotDuration;
    }

    private void verifyInOpeningTime(LocalTime beginTime, List<Opening> openingList, int appointmentDuration) {
        for (Opening opening : openingList) {
            if (inOpeningTime(opening.openingTime(), opening.closeTime(), beginTime, appointmentDuration))
                return;
        }

        log.error("BeginTime {} and appointment duration {} not in opening time", beginTime, appointmentDuration);
        throw new TakeAppointmentException(NOT_IN_OPENING_TIME_ERROR);
    }

    private boolean inOpeningTime(LocalTime openingTime, LocalTime closeTime, LocalTime timeToVerify, int appointmentDuration) {
        return (openingTime.equals(timeToVerify) || openingTime.isBefore(timeToVerify)) && (timeToVerify.isBefore(closeTime) &&
                minuteBetween(timeToVerify, closeTime) >= appointmentDuration);
    }

    private void verifyInstituteIsOpen(List<Opening> openingList) {
        if (openingList == null || openingList.isEmpty()) {
            log.error("Institute not open for this day");
            throw new TakeAppointmentException(NOT_OPEN_ERROR);
        }
    }

    private void verifyDayOrBeginTime(LocalDate day, LocalTime beginTime) throws IllegalArgumentException {
        if (day == null || beginTime == null) {
            log.error("Day or beginTime is null");
            throw new TakeAppointmentException(NULL_DAY_OR_BEGIN_TIME_ERROR);
        }
    }

    private Client verifyClient(int idClient) {
        Client c = clientRepository.findByIdPerson(idClient);
        if (c == null) {
            log.error("Not find client for idClient {}", idClient);
            throw new TakeAppointmentException(CLIENT_ID_NOT_FOUND_ERROR);
        }
        return c;
    }

    private Technician verifyTechnician(int idTechnician) {
        Technician t = technicianRepository.findByIdPerson(idTechnician);
        if (t == null) {
            log.error("Not find technician for idTechnician {}", idTechnician);
            throw new TakeAppointmentException(TECHNICIAN_ID_NOT_FOUND_ERROR);
        }
        return t;
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        if (ac == null) {
            log.error("Not find aesthetic care for idAestheticCare {}", idAestheticCare);
            throw new TakeAppointmentException(AESTHETIC_CARE_ID_NOT_FOUND_ERROR);
        }
        return ac;
    }

    private boolean thereIsOverlap(TimeSlot ts) {
        List<TimeSlot> tsOfTheDay = timeSlotRepository.findByTechnicianAndDay(ts.getTechnician(), ts.getDay());
        if (tsOfTheDay != null)
            for (TimeSlot timeSlot : tsOfTheDay)
                if (hasOverlap(timeSlot, ts.getBegin(), ts.getTime()))
                    return true;

        return false;
    }

    private boolean hasOverlap(TimeSlot existingTS, LocalTime beginTime, int lockTime) {
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
                // new time slot is equal to existing time slot
                log.debug("There is overlap because time slot are equals, begin time = {}", beginTime);
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
    private boolean hasOverlap(LocalTime beforeBeginTime, int beforeLockTime, LocalTime afterBeginTime) {
        long minuteDiff = minuteBetween(beforeBeginTime, afterBeginTime);
        if (minuteDiff < beforeLockTime) {
            log.debug("Overlap between before TS beginTime {} lockTime {} and after TS beginTime {}", beforeBeginTime, beforeLockTime,
                      afterBeginTime);
            return true;
        }

        return false;
    }

    private TimeSlot buildTimeSlotWithoutAppointment(Technician technician, LocalDate day, LocalTime beginTime, int timeExecution) {
        return TimeSlot.builder()
                .day(day)
                .begin(beginTime)
                .time(timeExecution)
                .free(false)
                .technician(technician)
                .build();
    }

    private Appointment buildAppointment(Client client, Technician technician, AestheticCare aestheticCare, LocalDate day, LocalTime time) {
        return Appointment.builder()
                .client(client)
                .technician(technician)
                .aestheticCare(aestheticCare)
                .canceled(false)
                .day(day)
                .time(time)
                .build();
    }

    private Appointment saveAppointment(Appointment appointment) {
        Appointment a = appointmentRepository.save(appointment);
        log.info("Save appointment {}", a);
        return a;
    }

    /**
     * Create a launch break for the technician if it needed. The launch break is created only if in the launch break time zone there is only one ts
     * free.
     *
     * @param technician  the technician
     * @param day         the day
     * @param tsaDuration ts duration
     */
    private void lockLaunchBreak(Technician technician, LocalDate day, int tsaDuration) {
        LaunchBreak launchBreak = launchBreakRepository.findByTechnicianAndDay(technician, day);
        if (launchBreak == null) {
            List<TimeSlot> tsInLaunchBreakTimeZone = getTimeSlotInLaunchBreakTimeZone(technician, day);
            createLaunchBreakIfNeeded(technician, day, tsaDuration, tsInLaunchBreakTimeZone);
        }
    }

    /**
     * @param technician the technician
     * @param day        the day
     *
     * @return a list which contains only ts associated to the technician not free and in the Launch Break time zone.
     */
    private List<TimeSlot> getTimeSlotInLaunchBreakTimeZone(Technician technician, LocalDate day) {
        List<TimeSlot> techTS = timeSlotRepository.findByTechnicianAndDay(technician, day);

        // Get all not free technician ts in launch break TZ
        Set<LocalTime> techTsLocalTimeSet = new HashSet<>();
        List<TimeSlot> techTsInLaunchBreakTZ = techTS.stream().filter(ts -> !ts.isFree())
                .filter(ts -> technicianLaunchBreakService.isInLaunchBreakTimeZone(ts.getBegin(), ts.getTime())).toList();
        techTsInLaunchBreakTZ.forEach(ts -> techTsLocalTimeSet.add(ts.getBegin()));

        // Get all global ts in opening time which are in launch break TZ
        List<TimeSlot> allTsInLaunchBreakTZ = openingService.allTimeSlotOfTheDay(day).stream()
                .filter(ts -> technicianLaunchBreakService.isInLaunchBreakTimeZone(ts.getBegin(), ts.getTime())).toList();

        // Set not free all global ts which are not free for the technician
        for (TimeSlot ts : allTsInLaunchBreakTZ) {
            if (techTsLocalTimeSet.contains(ts.getBegin())) {
                // Is a technician ts not free.
                ts.setFree(false);
            }
        }

        return allTsInLaunchBreakTZ;
    }

    private void createLaunchBreakIfNeeded(Technician technician, LocalDate day, int tsaDuration, List<TimeSlot> tsInLaunchBreakTimeZone) {
        int freeTsCount = 0;
        TimeSlot firstFree = null;
        for (TimeSlot timeSlot : tsInLaunchBreakTimeZone) {
            if (timeSlot.isFree()) {
                freeTsCount++;
                if (firstFree == null)
                    firstFree = timeSlot;
            }
        }

        if (freeTsCount == 1)
            technicianLaunchBreakService.createTechnicianLaunchBreak(technician.getIdPerson(), day, firstFree.getBegin(), tsaDuration);
    }

    private void notifyAppointmentCreate(Client client, Technician technician, LocalDate day, LocalTime beginTime) {
        boolean success = notificationCreationService.createNotification(NOTIFICATION_APPOINTMENT_CREATION_TITLE,
                                                                         NOTIFICATION_APPOINTMENT_CREATION_TEXT.formatted(day, beginTime,
                                                                                                                          client.simplyIdentifier(),
                                                                                                                          technician.simplyIdentifier()),
                                                                         BOSS_SECRETARY_LEVEL, false);
        if (!success)
            log.error("Fail to create appointment notification");
    }

    // Exceptions.

    private static class TakeAppointmentException extends RuntimeException {
        public TakeAppointmentException(String message) {
            super(message);
        }
    }
}
