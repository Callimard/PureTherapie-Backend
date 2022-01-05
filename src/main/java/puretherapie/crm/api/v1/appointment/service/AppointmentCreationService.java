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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static puretherapie.crm.data.notification.NotificationLevel.BOSS_SECRETARY_LEVEL;
import static puretherapie.crm.tool.TimeTool.minuteBetween;

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
    public boolean createAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime beginTime) {
        return createAppointment(idClient, idTechnician, idAestheticCare, day, beginTime, false);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createAppointment(int idClient, int idTechnician, int idAestheticCare, LocalDate day, LocalTime beginTime,
                                     boolean overlapAuthorized) {
        try {
            verifyDayOrBeginTime(day, beginTime);

            Client client = verifyClient(idClient);
            Technician technician = verifyTechnician(idTechnician);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);

            List<TimeSlotAtom> tsaList = tsaRepository.findAllByOrderByEffectiveDate();

            List<Opening> openingList = getOpenings(day);
            TimeSlotAtom tsa = searchCorrectTSA(tsaList, day);
            int appointmentDuration = computeAppointmentDuration(aestheticCare, tsa, overlapAuthorized);

            if (instituteIsOpen(openingList)) {
                if (appointmentInOpeningTime(beginTime, openingList, appointmentDuration)) {
                    if (appointmentNotDuringLaunchBreak(technician, day, beginTime, appointmentDuration)) {
                        if (isCompatibleTimeSlotTime(beginTime, openingList, tsa)) {
                            int nbTimeSlot = getNbTimeSlot(aestheticCare, overlapAuthorized, tsa);
                            List<TimeSlot> timeSlots = createTimeSlot(technician, day, beginTime, tsa, nbTimeSlot);
                            if (noOverlapForAll(timeSlots)) {
                                Appointment appointment = buildAppointment(client, technician, aestheticCare);
                                saveAppointment(appointment);
                                saveAllTimeSlot(timeSlots, appointment);
                                notifyAppointmentCreate(client, technician, beginTime);
                            } else {
                                log.debug("Fail to create appointment, time slot are not free for the begin time {}", beginTime);
                                return false;
                            }
                        } else {
                            log.debug("Fail to create appointment, incompatible time slot time with time slot duration as {} minutes",
                                      tsa.getNumberOfMinutes());
                            return false;
                        }
                    } else {
                        log.debug("Fail to create appointment, time {} is during technician launch break", beginTime);
                    }
                } else {
                    log.debug("Fail to create appointment, time {} out of opening time", beginTime);
                    return false;
                }
            } else {
                log.debug("Fail to create appointment, institute is closed at this day {}", day);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.debug("Fail to create appointment", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private void saveAllTimeSlot(List<TimeSlot> timeSlots, Appointment appointment) {
        for (TimeSlot timeSlot : timeSlots) {
            timeSlot.setAppointment(appointment);
            TimeSlot ts = timeSlotRepository.save(timeSlot);
            log.debug("Save time slot {}", ts);
        }
    }

    private boolean noOverlapForAll(List<TimeSlot> timeSlots) {
        for (TimeSlot timeSlot : timeSlots)
            if (thereIsOverlap(timeSlot))
                return false;

        return true;
    }

    private List<TimeSlot> createTimeSlot(Technician technician, LocalDate day, LocalTime beginTime, TimeSlotAtom tsa, int nbTimeSlot) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        LocalTime time = beginTime;
        for (int i = 0; i < nbTimeSlot; i++) {
            timeSlots.add(buildTimeSlotWithoutAppointment(technician, day, time, tsa.getNumberOfMinutes()));
            time = time.plusMinutes(tsa.getNumberOfMinutes());
        }

        return timeSlots;
    }

    private boolean isCompatibleTimeSlotTime(LocalTime beginTime, List<Opening> openingList, TimeSlotAtom tsa) {
        return allCorrectTimeSlotTimes(openingList, tsa).contains(beginTime);
    }

    private Set<LocalTime> allCorrectTimeSlotTimes(List<Opening> openingList, TimeSlotAtom tsa) {
        Set<LocalTime> correctTimeSlotTimes = new HashSet<>();
        for (Opening opening : openingList) {
            correctTimeSlotTimes.addAll(opening.correctTimeSlotTime(tsa));
        }
        return correctTimeSlotTimes;
    }

    private boolean appointmentNotDuringLaunchBreak(Technician technician, LocalDate appointmentDay, LocalTime appointmentBeginTime,
                                                    int appointmentDuration) {
        LaunchBreak technicianLaunchBreak = lbRepository.findByTechnicianAndDay(technician, appointmentDay);
        LocalTime launchBreakBegin = technicianLaunchBreak.getBeginHour();
        int launchBreakDuration = technicianLaunchBreak.getDuration();
        return (appointmentBeginTime.isBefore(launchBreakBegin) && minuteBetween(appointmentBeginTime, launchBreakBegin) >= appointmentDuration)
                ||
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

    private int computeAppointmentDuration(AestheticCare aestheticCare, TimeSlotAtom tsa, boolean authorizedOverlap) {
        return getNbTimeSlot(aestheticCare, authorizedOverlap, tsa) * tsa.getNumberOfMinutes();
    }

    private int getNbTimeSlot(AestheticCare aestheticCare, boolean authorizedOverlap, TimeSlotAtom chosenTSA) {
        int timeSlotDuration = chosenTSA.getNumberOfMinutes();
        int acExecutionTime = aestheticCare.getTimeExecution();
        int nbTimeSlot = 1;
        int rest = 0;

        if (isMoreThanOneTimeSlot(timeSlotDuration, acExecutionTime)) {
            nbTimeSlot = acExecutionTime / timeSlotDuration;
            rest = acExecutionTime % timeSlotDuration;
        }

        if (!authorizedOverlap && rest > 0)
            nbTimeSlot += 1;
        return nbTimeSlot;
    }

    private boolean isMoreThanOneTimeSlot(int timeSlotDuration, int acExecutionTime) {
        return acExecutionTime > timeSlotDuration;
    }

    private boolean appointmentInOpeningTime(LocalTime beginTime, List<Opening> openingList, int appointmentDuration) {
        for (Opening opening : openingList) {
            if (inOpeningTime(opening.openingTime(), opening.closeTime(), beginTime, appointmentDuration))
                return true;
        }

        return false;
    }

    private boolean inOpeningTime(LocalTime openingTime, LocalTime closeTime, LocalTime timeToVerify, int appointmentDuration) {
        return (openingTime.equals(timeToVerify) || openingTime.isAfter(timeToVerify)) && timeToVerify.isBefore(closeTime) &&
                minuteBetween(timeToVerify, closeTime) >= appointmentDuration;
    }

    private boolean instituteIsOpen(List<Opening> openingList) {
        return openingList != null && !openingList.isEmpty();
    }

    private void verifyDayOrBeginTime(LocalDate day, LocalTime beginTime) throws IllegalArgumentException {
        if (day == null || beginTime == null) {
            log.debug("Day or beginTime is null");
            throw new IllegalArgumentException("Day or BeginTime is null");
        }
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

    private boolean thereIsOverlap(TimeSlot ts) {
        List<TimeSlot> tsOfTheDay = timeSlotRepository.findByTechnicianAndDay(ts.getTechnician(), ts.getDay());
        if (tsOfTheDay != null)
            for (TimeSlot timeSlot : tsOfTheDay)
                if (hasOverlap(timeSlot, ts.getBegin(), ts.getTime(), 0))
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
        long minuteDiff = minuteBetween(beforeBeginTime, afterBeginTime);
        if (minuteDiff < beforeLockTime - overlapAuthorized) {
            log.debug(
                    "Overlap between before TS beginTime {} lockTime {} and after TS beginTime {}", beforeBeginTime, beforeLockTime, afterBeginTime);
            return true;
        }

        if (minuteDiff < beforeLockTime)
            log.info("Sur booking done with a overlap of {} minutes", (beforeLockTime - minuteDiff));

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

    private void saveAppointment(Appointment appointment) {
        Appointment a = appointmentRepository.save(appointment);
        log.info("Save appointment {}", a);
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
}
