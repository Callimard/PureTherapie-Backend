package puretherapie.crm.api.v1.agenda.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.agenda.controller.dto.FreeTimeSlotDTO;
import puretherapie.crm.api.v1.agenda.controller.dto.TimeSlotDTO;
import puretherapie.crm.api.v1.agenda.service.OpeningService;
import puretherapie.crm.api.v1.agenda.service.TimeSlotAtomService;
import puretherapie.crm.api.v1.person.technician.service.TechnicianAbsenceService;
import puretherapie.crm.api.v1.person.technician.service.TechnicianLaunchBreakService;
import puretherapie.crm.api.v1.person.technician.service.TechnicianService;
import puretherapie.crm.data.agenda.Opening;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.person.technician.LaunchBreak;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.TechnicianAbsence;
import puretherapie.crm.data.person.technician.repository.LaunchBreakRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianAbsenceRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.agenda.controller.AgendaController.AGENDA_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(AGENDA_URL)
public class AgendaController {

    // Constants.

    public static final String AGENDA_URL = API_V1_URL + "/agenda";

    public static final String TECHNICIAN_FREE_TIME_SLOTS = "/technicians/{idTechnician}/free_time_slots";
    public static final String TECHNICIAN_FREE_TIME_SLOTS_URL = AGENDA_URL + TECHNICIAN_FREE_TIME_SLOTS;

    public static final String TIME_SLOTS = "/time_slots";
    public static final String TIME_SLOTS_URL = AGENDA_URL + TIME_SLOTS;
    public static final String ALL_TECHNICIAN_TIME_SLOTS = "/technicians/{idTechnician}/time_slots";
    public static final String ALL_TECHNICIAN_TIME_SLOTS_URL = AGENDA_URL + ALL_TECHNICIAN_TIME_SLOTS;

    public static final String ALL_DAY_TECHNICIAN_TIME_SLOTS = "/technicians" + TIME_SLOTS;
    public static final String ALL_DAY_TECHNICIAN_TIME_SLOTS_URL = AGENDA_URL + ALL_DAY_TECHNICIAN_TIME_SLOTS;

    // Variables.

    private final TechnicianRepository technicianRepository;
    private final TechnicianAbsenceService technicianAbsenceService;
    private final TechnicianService technicianService;
    private final TechnicianLaunchBreakService technicianLaunchBreakService;
    private final OpeningService openingService;
    private final TimeSlotAtomService timeSlotAtomService;
    private final TechnicianAbsenceRepository technicianAbsenceRepository;
    private final LaunchBreakRepository launchBreakRepository;

    // Methods.

    @GetMapping(TECHNICIAN_FREE_TIME_SLOTS)
    public List<FreeTimeSlotDTO> getTechnicianFreeTimeSlots(@PathVariable(name = "idTechnician") int idTechnician, @RequestParam(name = "day")
            String day, @RequestParam(name = "processDuration") int processDuration) {
        return technicianService.getTechnicianFreeTimeSlot(idTechnician, LocalDate.parse(day), processDuration);
    }

    /**
     * @param idTechnician the technician id
     * @param date         the date
     *
     * @return the list of {@link TimeSlotDTO} of all the date (in function of opening and close time) with technician time slot occupied and free.
     */
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(ALL_TECHNICIAN_TIME_SLOTS)
    public List<TimeSlotDTO> getAllTechnicianTimeSlots(@PathVariable(name = "idTechnician") int idTechnician,
                                                       @RequestParam(name = "date") String date) {
        try {
            LocalDate day = LocalDate.parse(date);
            if (openingService.isOpen(day)) {

                Technician technician = technicianRepository.findByIdPerson(idTechnician);

                int tsaNumberOfMinutes = timeSlotAtomService.searchCorrectTSA(day).getNumberOfMinutes();

                List<TimeSlotDTO> technicianTS = technicianAbsenceService.getTechnicianNotFreeTimeSlot(idTechnician, day);

                Set<LocalTime> setCorrectBeginTS = new HashSet<>(technicianTS.stream().map(ts -> LocalTime.parse(ts.getBegin())).toList());
                List<TimeSlotDTO> allTS = new ArrayList<>(technicianTS);

                List<Opening> openings = openingService.getOpenings(day);

                List<TechnicianAbsence> technicianAbsences = technicianAbsenceRepository.findByTechnicianAndDay(technician, day);
                LaunchBreak launchBreak = launchBreakRepository.findByTechnicianAndDay(technician, day);

                for (Opening opening : openings) {
                    List<LocalTime> correctTS = Opening.correctTimeSlotTime(opening, tsaNumberOfMinutes);
                    for (LocalTime lt : correctTS) {
                        if (setCorrectBeginTS.add(lt)) {
                            TimeSlotDTO ts = TimeSlotDTO.builder()
                                    .technician(technician.transform())
                                    .day(date)
                                    .begin(lt.toString())
                                    .duration(tsaNumberOfMinutes)
                                    .free(true)
                                    .isLaunchBreak(false)
                                    .isAbsence(false)
                                    .build();

                            if (technicianAbsenceService.isInTechnicianAbsence(technicianAbsences, lt, tsaNumberOfMinutes)) {
                                ts.setAbsence(true);
                            }

                            if (technicianLaunchBreakService.isDuringTechnicianLaunchBreak(launchBreak, lt, tsaNumberOfMinutes)) {
                                ts.setLaunchBreak(true);
                            }

                            allTS.add(ts);
                        }
                    }
                }

                Collections.sort(allTS);
                allTS.remove(allTS.size() - 1); // Remove the last which is the closing time.

                if (allTS.isEmpty())
                    log.error("All Time Slots is empty and it is not normal for the technician id {} and the day {}", idTechnician, day);

                return allTS;
            } else {
                log.debug("No technician time slot for the date {} because not open", date);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error get all T TS", e);
            return Collections.emptyList();
        }
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(ALL_DAY_TECHNICIAN_TIME_SLOTS)
    public List<TimeSlotDTO> getAllDayTechnicianTimeSlots(@RequestParam(name = "date") String date) {
        List<TimeSlotDTO> allTechTS = new ArrayList<>();

        List<Technician> technicians = technicianRepository.findByActive(true);

        for (Technician technician : technicians) {
            allTechTS.addAll(getAllTechnicianTimeSlots(technician.getIdPerson(), date));
        }

        return allTechTS;
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(TIME_SLOTS)
    public List<TimeSlotDTO> getAllTimeSlotsOfTheDay(@RequestParam(value = "date") String date) {
        LocalDate day = LocalDate.parse(date);
        if (openingService.isOpen(day)) {
            List<TimeSlotDTO> timeSlots = openingService.allTimeSlotOfTheDay(day).stream().map(TimeSlot::transform).toList();

            if (timeSlots.isEmpty())
                log.error("List time slots DTO is empty whereas the institute is opened at the day {}", day);

            return timeSlots;
        } else {
            log.debug("No time slot for the date {} because not open", date);
            return Collections.emptyList();
        }
    }

}
