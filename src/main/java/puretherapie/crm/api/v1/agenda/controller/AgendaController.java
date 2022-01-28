package puretherapie.crm.api.v1.agenda.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.agenda.controller.dto.FreeTimeSlotDTO;
import puretherapie.crm.api.v1.agenda.controller.dto.TimeSlotDTO;
import puretherapie.crm.api.v1.agenda.service.OpeningService;
import puretherapie.crm.api.v1.agenda.service.TimeSlotAtomService;
import puretherapie.crm.api.v1.person.technician.service.TechnicianService;
import puretherapie.crm.data.agenda.Opening;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(AgendaController.AGENDA_URL)
public class AgendaController {

    // Constants.

    public static final String AGENDA_URL = API_V1_URL + "/agenda";

    public static final String TECHNICIAN_FREE_TIME_SLOTS = "/technicians/free_time_slots";
    public static final String TECHNICIAN_FREE_TIME_SLOTS_URL = AGENDA_URL + TECHNICIAN_FREE_TIME_SLOTS;

    public static final String DAY_ALL_TIME_SLOTS = "/time_slots";
    public static final String DAY_ALL_TIME_SLOTS_URL = AGENDA_URL + DAY_ALL_TIME_SLOTS;
    public static final String DAY_ALL_TECHNICIAN_TIME_SLOTS = "/technician/time_slots";
    public static final String DAY_ALL_TECHNICIAN_TIME_SLOTS_URL = AGENDA_URL + DAY_ALL_TECHNICIAN_TIME_SLOTS;

    // Variables.

    private final TechnicianService technicianService;
    private final OpeningService openingService;
    private final TimeSlotAtomService timeSlotAtomService;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @GetMapping(TECHNICIAN_FREE_TIME_SLOTS)
    public List<FreeTimeSlotDTO> getTechnicianFreeTimeSlots(@RequestParam(name = "idTechnician") int idTechnician, @RequestParam(name = "day")
            String day, @RequestParam(name = "processDuration") int processDuration) {
        return technicianService.getTechnicianFreeTimeSlot(idTechnician, LocalDate.parse(day), processDuration);
    }

    /**
     * @param idTechnician the technician id
     * @param date         the date
     *
     * @return the list of {@link TimeSlotDTO} of all the date (in function of opening and close time) with technician time slot occupied and free.
     */
    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(DAY_ALL_TECHNICIAN_TIME_SLOTS)
    public List<TimeSlotDTO> getAllTechnicianTimeSlots(@RequestParam(name = "idTechnician") int idTechnician,
                                                       @RequestParam(name = "date") String date) {
        try {
            LocalDate day = LocalDate.parse(date);
            if (openingService.isOpen(day)) {
                int tsaNumberOfMinutes = timeSlotAtomService.searchCorrectTSA(day).getNumberOfMinutes();

                List<TimeSlotDTO> technicianTS = technicianService.getTechnicianOccupiedTimeSlot(idTechnician, day);

                Set<LocalTime> setCorrectBeginTS = new HashSet<>(technicianTS.stream().map(ts -> LocalTime.parse(ts.getBegin())).toList());
                List<TimeSlotDTO> allTS = new ArrayList<>(technicianTS);

                List<Opening> openings = openingService.getOpenings(day);

                for (Opening opening : openings) {
                    List<LocalTime> correctTS = Opening.correctTimeSlotTime(opening, tsaNumberOfMinutes);
                    for (LocalTime lt : correctTS) {
                        if (setCorrectBeginTS.add(lt)) {
                            TimeSlotDTO ts = TimeSlotDTO.builder()
                                    .day(date)
                                    .begin(lt.toString())
                                    .time(tsaNumberOfMinutes)
                                    .free(true)
                                    .isLaunchBreak(false)
                                    .isAbsence(false)
                                    .build();
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

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(DAY_ALL_TIME_SLOTS)
    public List<TimeSlotDTO> getAllTimeSlotsOfTheDay(@RequestParam(value = "date") String date) {
        LocalDate day = LocalDate.parse(date);
        if (openingService.isOpen(day)) {
            int tsaNumberOfMinutes = timeSlotAtomService.searchCorrectTSA(day).getNumberOfMinutes();

            List<Opening> openings = openingService.getOpenings(day);

            Set<LocalTime> setCorrectBeginTS = new HashSet<>();
            for (Opening opening : openings)
                setCorrectBeginTS.addAll(Opening.correctTimeSlotTime(opening, tsaNumberOfMinutes));

            List<LocalTime> listCorrectBeginTS = new ArrayList<>(setCorrectBeginTS.stream().toList());
            Collections.sort(listCorrectBeginTS);

            List<TimeSlotDTO> timeSlots = new ArrayList<>();
            for (LocalTime lt : listCorrectBeginTS) {
                TimeSlotDTO ts = TimeSlotDTO.builder()
                        .day(date)
                        .begin(lt.toString())
                        .time(tsaNumberOfMinutes)
                        .free(true)
                        .isLaunchBreak(false)
                        .isAbsence(false)
                        .build();
                timeSlots.add(ts);
            }

            if (timeSlots.isEmpty())
                log.error("List time slots DTO is empty whereas the institute is opened at the day {}", day);

            return timeSlots;
        } else {
            log.debug("No time slot for the date {} because not open", date);
            return Collections.emptyList();
        }
    }

}
