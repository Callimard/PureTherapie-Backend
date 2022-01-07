package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.data.agenda.*;
import puretherapie.crm.data.agenda.repository.*;
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
import puretherapie.crm.tool.ServiceTool;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static puretherapie.crm.data.agenda.Opening.correctTimeSlotTime;
import static puretherapie.crm.data.notification.NotificationLevel.BOSS_SECRETARY_LEVEL;
import static puretherapie.crm.tool.ServiceTool.generateError;
import static puretherapie.crm.tool.TimeTool.minuteBetween;

@Slf4j
@AllArgsConstructor
@Service
public class TakeAppointmentService {

    // Constants.

    private static final String NOTIFICATION_APPOINTMENT_CREATION_TITLE = "New appointment for client %s";
    private static final String NOTIFICATION_APPOINTMENT_CREATION_TEXT = "New appointment at %s for the client %s with the technician %s";

    // Success fields.

    public static final String APPOINTMENT_CREATION_SUCCESS = "appointment_creation_success";

    // Error fields.

    public static final String APPOINTMENT_CREATION_FAIL = "appointment_creation_fail";

    public static final String UNKNOWN_ERROR = "unknown_error";
    public static final String NULL_DAY_OR_BEGIN_TIME_ERROR = "null_day_begin_time";
    public static final String CLIENT_ID_NOT_FOUND_ERROR = "client_id_not_found";
    public static final String TECHNICIAN_ID_NOT_FOUND_ERROR = "technician_id_not_found";
    public static final String AESTHETIC_CARE_ID_NOT_FOUND_ERROR = "aesthetic_care_id_not_found";
    public static final String EXCEPTIONAL_CLOSE_ERROR = "exceptional_close";
    public static final String NOT_OPEN_ERROR = "not_open";
    public static final String NOT_IN_OPENING_TIME_ERROR = "not_in_opening_time";
    public static final String DURING_LAUNCH_BREAK_ERROR = "during_launch_break";
    public static final String INCOMPATIBLE_TIME_SLOT_TIME = "incompatible_time_slot_time";
    public static final String OVERLAP_ERROR = "overlap_detect";

    // Variables.

    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotAtomRepository tsaRepository;
    private final ExceptionalOpeningRepository eoRepository;
    private final ExceptionalCloseRepository ecRepository;
    private final GlobalOpeningTimeRepository gotRepository;
    private final LaunchBreakRepository lbRepository;
    private final NotificationCreationService notificationCreationService;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> takeAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime beginTime) {
        return takeAppointment(idClient, idTechnician, idAestheticCare, day, beginTime, false);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> takeAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime beginTime,
                                               boolean overlapAuthorized) {
        try {
            verifyDayOrBeginTime(day, beginTime);

            Client client = verifyClient(idClient);
            Technician technician = verifyTechnician(idTechnician);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);

            List<TimeSlotAtom> tsaList = tsaRepository.findAllByOrderByEffectiveDate();

            List<Opening> openingList = getOpenings(day);
            TimeSlotAtom tsa = searchCorrectTSA(tsaList, day);
            int nbTimeSlot = getNbTimeSlot(aestheticCare, tsa.getNumberOfMinutes(), overlapAuthorized);
            int appointmentDuration = computeAppointmentDuration(nbTimeSlot, tsa.getNumberOfMinutes());

            verifyExceptionalClose(day);
            verifyInstituteIsOpen(openingList);
            verifyInOpeningTime(beginTime, openingList, appointmentDuration);
            verifyNotDuringLaunchBreak(technician, day, beginTime, appointmentDuration);
            verifyIsCompatibleTimeSlotTime(beginTime, openingList, tsa.getNumberOfMinutes());
            List<TimeSlot> allTimeSlots = generateAllTimeSlots(technician, day, beginTime, tsa.getNumberOfMinutes(), nbTimeSlot);
            verifyNoOverlapForAll(allTimeSlots);
            Appointment appointment = buildAppointment(client, technician, aestheticCare);
            appointment = saveAppointment(appointment);
            saveAllTimeSlot(allTimeSlots, appointment);
            notifyAppointmentCreate(client, technician, beginTime);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to create appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private Map<String, Object> generateSuccessRes() {
        return Collections.singletonMap(APPOINTMENT_CREATION_SUCCESS, "Appointment creation success");
    }

    private Map<String, Object> generateErrorRes(Exception e) {
        if (e instanceof AppointmentCreationException ace) {
            return Collections.singletonMap(APPOINTMENT_CREATION_FAIL, ace.getErrors());
        } else {
            return Collections.singletonMap(UNKNOWN_ERROR, e.getMessage());
        }
    }

    private void verifyExceptionalClose(LocalDate day) {
        if (ecRepository.findByDay(day) != null) {
            log.debug("Exception close detect for the day {}", day);
            throw new AppointmentCreationException("Exceptional close at day %s".formatted(day),
                                                   generateError(EXCEPTIONAL_CLOSE_ERROR, "Exceptional close at %s".formatted(day)));
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
                log.debug("Found overlap for time slot {}", timeSlot);
                throw new AppointmentCreationException("Find an overlap for one time slot", generateError(OVERLAP_ERROR, "Overlap detected"));
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
            log.debug("Incompatible time slot time for begin time {}", beginTime);
            throw new AppointmentCreationException("Incompatible time slot time", generateError(INCOMPATIBLE_TIME_SLOT_TIME, "Incompatible time " +
                    "slot time for begin time %s".formatted(beginTime)));
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

    private void verifyNotDuringLaunchBreak(Technician technician, LocalDate appointmentDay, LocalTime appointmentBeginTime,
                                            int appointmentDuration) {
        LaunchBreak technicianLaunchBreak = lbRepository.findByTechnicianAndDay(technician, appointmentDay);
        LocalTime launchBreakBegin = technicianLaunchBreak.getBeginHour();
        int launchBreakDuration = technicianLaunchBreak.getDuration();
        if (notInLaunchBreak(appointmentBeginTime, appointmentDuration, launchBreakBegin, launchBreakDuration))
            return;

        log.debug("In launch break of the technician {}, appointment time = {}", technician.simplyIdentifier(), appointmentBeginTime);
        throw new AppointmentCreationException("In launch break", generateError(DURING_LAUNCH_BREAK_ERROR, "In technician launch break"));
    }

    private boolean notInLaunchBreak(LocalTime appointmentBeginTime, int appointmentDuration, LocalTime launchBreakBegin, int launchBreakDuration) {
        return (appointmentBeginTime.isBefore(launchBreakBegin) && minuteBetween(appointmentBeginTime, launchBreakBegin) >= appointmentDuration) ||
                (launchBreakBegin.isBefore(appointmentBeginTime) && minuteBetween(launchBreakBegin, appointmentBeginTime) >= launchBreakDuration);
    }

    private List<Opening> getOpenings(LocalDate day) {
        List<ExceptionalOpening> eoList = eoRepository.findByDay(day);
        List<GlobalOpeningTime> gotList = gotRepository.findByDay(day.getDayOfWeek().getValue());

        List<Opening> openingList = new ArrayList<>();
        openingList.addAll(eoList);
        openingList.addAll(gotList);
        return openingList;
    }

    private TimeSlotAtom searchCorrectTSA(List<TimeSlotAtom> tsaList, LocalDate day) {
        verifyTSAList(tsaList);
        return searchCorrectTSA(tsaList, day, tsaList.get(0));
    }

    private void verifyTSAList(List<TimeSlotAtom> tsaList) {
        if (tsaList == null || tsaList.isEmpty())
            throw new IllegalArgumentException("TimeSlotAtom list is null");
    }

    private TimeSlotAtom searchCorrectTSA(List<TimeSlotAtom> tsaList, LocalDate day, TimeSlotAtom chosenTSA) {
        if (!chosenTSA.getEffectiveDate().isBefore(day))
            for (int i = 1; i < tsaList.size(); i++) {
                TimeSlotAtom current = tsaList.get(i);
                if (current.getEffectiveDate().isBefore(day))
                    return current;
            }

        return chosenTSA;
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

        log.debug("BeginTime {} and appointment duration {} not in opening time", beginTime, appointmentDuration);
        throw new AppointmentCreationException("Not in opening time (beginTime = %s, duration = %s)".formatted(beginTime, appointmentDuration),
                                               generateError(NOT_IN_OPENING_TIME_ERROR, "Not in opening time"));
    }

    private boolean inOpeningTime(LocalTime openingTime, LocalTime closeTime, LocalTime timeToVerify, int appointmentDuration) {
        return (openingTime.equals(timeToVerify) || openingTime.isBefore(timeToVerify)) && (timeToVerify.isBefore(closeTime) &&
                minuteBetween(timeToVerify, closeTime) >= appointmentDuration);
    }

    private void verifyInstituteIsOpen(List<Opening> openingList) {
        if (openingList == null || openingList.isEmpty()) {
            log.debug("Institute not open for this day");
            throw new AppointmentCreationException("Not open", generateError(NOT_OPEN_ERROR, "Institute not open"));
        }
    }

    private void verifyDayOrBeginTime(LocalDate day, LocalTime beginTime) throws IllegalArgumentException {
        if (day == null || beginTime == null) {
            log.debug("Day or beginTime is null");
            throw new AppointmentCreationException("Day or BeginTime is null",
                                                   generateError(NULL_DAY_OR_BEGIN_TIME_ERROR, "Null pointer of day or begin " +
                                                           "time"));
        }
    }

    private Client verifyClient(int idClient) {
        Client c = clientRepository.findByIdPerson(idClient);
        if (c == null)
            throw new AppointmentCreationException("Not find client for idClient %s".formatted(idClient), generateError(CLIENT_ID_NOT_FOUND_ERROR,
                                                                                                                        "Client id not found"));
        return c;
    }

    private Technician verifyTechnician(int idTechnician) {
        Technician t = technicianRepository.findByIdPerson(idTechnician);
        if (t == null)
            throw new AppointmentCreationException("Not find technician for idTechnician %s".formatted(idTechnician),
                                                   generateError(TECHNICIAN_ID_NOT_FOUND_ERROR, "Technician id not found"));
        return t;
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        if (ac == null)
            throw new AppointmentCreationException("Not find aesthetic care for idAestheticCare %s".formatted(idAestheticCare),
                                                   generateError(AESTHETIC_CARE_ID_NOT_FOUND_ERROR, "Aesthetic care id not found"));
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

    private Appointment buildAppointment(Client client, Technician technician, AestheticCare aestheticCare) {
        return Appointment.builder()
                .client(client)
                .technician(technician)
                .aestheticCare(aestheticCare)
                .canceled(false)
                .build();
    }

    private Appointment saveAppointment(Appointment appointment) {
        Appointment a = appointmentRepository.save(appointment);
        log.info("Save appointment {}", a);
        return a;
    }

    private void notifyAppointmentCreate(Client client, Technician technician, LocalTime beginTime) {
        boolean success = notificationCreationService.createNotification(NOTIFICATION_APPOINTMENT_CREATION_TITLE.formatted(client.simplyIdentifier()),
                                                                         NOTIFICATION_APPOINTMENT_CREATION_TEXT.formatted(beginTime,
                                                                                                                          client.simplyIdentifier(),
                                                                                                                          technician.simplyIdentifier()),
                                                                         BOSS_SECRETARY_LEVEL, false);
        if (!success)
            log.error("Fail to create appointment notification");
    }

    // Exceptions.

    private static class AppointmentCreationException extends ServiceTool.ServiceException {

        public AppointmentCreationException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }
}
