package puretherapie.crm.api.v1.person.technician.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.agenda.controller.dto.FreeTimeSlotDTO;
import puretherapie.crm.api.v1.agenda.service.OpeningService;
import puretherapie.crm.api.v1.agenda.service.TimeSlotAtomService;
import puretherapie.crm.data.agenda.Opening;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.TechnicianAbsence;
import puretherapie.crm.data.person.technician.repository.TechnicianAbsenceRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static puretherapie.crm.data.agenda.Opening.correctTimeSlotTime;

@Slf4j
@AllArgsConstructor
@Service
public class TechnicianService {

    // Variables.

    private final TechnicianAbsenceService technicianAbsenceService;

    private final TechnicianRepository technicianRepository;
    private final TechnicianAbsenceRepository technicianAbsenceRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotAtomService tsaService;
    private final OpeningService openingService;

    // Methods.

    public void activateTechnician(int idTechnician) {
        Technician technician = technicianRepository.findByIdPerson(idTechnician);
        if (!technician.isActive()) {
            technician.setActive(true);
            technician = technicianRepository.save(technician);
            log.info("Activate technician => {}", technician.simplyIdentifier());
        } else
            throw new TechnicianException("Technician already activated");
    }

    public void inactivateTechnician(int idTechnician) {
        Technician technician = technicianRepository.findByIdPerson(idTechnician);
        if (technician.isActive()) {
            technician.setActive(false);
            technician = technicianRepository.save(technician);
            log.info("Inactivate technician => {}", technician.simplyIdentifier());
        } else
            throw new TechnicianException("Technician already inactivated");
    }

    /**
     * Returns the list of all free Ts of the technician for the specified day and the specified process duration. The list returns will be different
     * based on the specified process duration. For example, free ts returned will be different if the process duration is 30minutes than 50minutes.
     *
     * @param idTechnician    the technician
     * @param day             the day
     * @param processDuration the process duration for which we want free ts
     *
     * @return the list of free ts coherent with the technician, the day and the process duration.
     */
    public List<FreeTimeSlotDTO> getTechnicianFreeTimeSlot(int idTechnician, LocalDate day, int processDuration) {
        Technician technician = verifyTechnician(idTechnician);
        verifyDay(day);

        if (openingService.isOpen(day)) {
            List<Opening> openingList = openingService.getOpenings(day);

            List<TimeSlot> existingTS = timeSlotRepository.findByTechnicianAndDay(technician, day);

            List<FreeTimeSlotDTO> freeTimeSlotDTOs = new ArrayList<>();
            for (Opening opening : openingList)
                freeTimeSlotDTOs.addAll(searchFreeTS(opening, tsaService.searchCorrectTSA(day).getNumberOfMinutes(), processDuration, existingTS,
                                                     idTechnician,
                                                     day));

            return freeTimeSlotDTOs;
        } else {
            return Collections.emptyList();
        }
    }

    private Technician verifyTechnician(int idTechnician) {
        Technician t = technicianRepository.findByIdPerson(idTechnician);
        if (t == null)
            throw new IllegalArgumentException("Not find technician for idTechnician %s".formatted(idTechnician));
        return t;
    }

    private void verifyDay(LocalDate day) {
        if (day == null)
            throw new IllegalArgumentException("Day cannot be null");
    }

    private Collection<FreeTimeSlotDTO> searchFreeTS(Opening opening, int tsDuration, int processDuration, List<TimeSlot> existingTS,
                                                     int idTechnician, LocalDate day) {
        int nbTimeSlotToOccupy = getNbTimeSlotToOccupy(tsDuration, processDuration);
        verifyNbTimeSlotToOccupy(nbTimeSlotToOccupy);

        List<FreeTimeSlotDTO> freeTimeSlotDTOList = getAllFreeTimeSlots(opening, tsDuration, existingTS, idTechnician, day, nbTimeSlotToOccupy);

        freeTimeSlotDTOList = filterWithTechnicianAbsence(idTechnician, day, freeTimeSlotDTOList);

        return filterWithCurrentTime(day, freeTimeSlotDTOList);
    }

    /**
     * Returns a list which does not contain ts which are in technician absence.
     *
     * @param idTechnician        the technician id
     * @param day                 the day
     * @param freeTimeSlotDTOList the list to filter
     *
     * @return a list which does not contain ts which are in technician absence
     */
    private List<FreeTimeSlotDTO> filterWithTechnicianAbsence(int idTechnician, LocalDate day, List<FreeTimeSlotDTO> freeTimeSlotDTOList) {
        Technician technician = technicianRepository.findByIdPerson(idTechnician);
        List<TechnicianAbsence> technicianAbsences = technicianAbsenceRepository.findByTechnicianAndDay(technician, day);

        return freeTimeSlotDTOList.stream().filter(freeTimeSlot -> {
            for (TechnicianAbsence technicianAbsence : technicianAbsences) {
                if (technicianAbsenceService.isInTechnicianAbsence(technicianAbsence, LocalTime.parse(freeTimeSlot.getBegin()),
                                                                   freeTimeSlot.getDuration())) {
                    return false;
                }
            }
            return true;
        }).toList();
    }

    private int getNbTimeSlotToOccupy(int tsDuration, int processDuration) {
        int nbTimeSlotToOccupy = processDuration / tsDuration;
        nbTimeSlotToOccupy += (processDuration % tsDuration) != 0 ? 1 : 0; // Compute if there is rest of the division.
        return nbTimeSlotToOccupy;
    }

    private void verifyNbTimeSlotToOccupy(int nbTimeSlotToOccupy) {
        if (nbTimeSlotToOccupy <= 0) {
            log.error("NbTimeSlot negative, nbTimeSlotToOccupy = {}", nbTimeSlotToOccupy);
            throw new IllegalArgumentException("Number of time slot negative => nbTimeSlotToOccupy = " + nbTimeSlotToOccupy);
        }
    }

    private List<FreeTimeSlotDTO> getAllFreeTimeSlots(Opening opening, int tsDuration, List<TimeSlot> existingTS, int idTechnician, LocalDate day,
                                                      int nbTimeSlotToOccupy) {
        List<FreeTimeSlotDTO> freeTimeSlotDTOList = new ArrayList<>();
        List<LocalTime> correctTSs = correctTimeSlotTime(opening, tsDuration);
        for (int i = 0; i < (correctTSs.size() - nbTimeSlotToOccupy); i++) {
            List<LocalTime> toOccupy = getTimeSlotToOccupy(correctTSs, i, nbTimeSlotToOccupy);
            if (canOccupyAllTimeSlots(toOccupy, existingTS))
                freeTimeSlotDTOList.add(buildFreeTimeSlot(idTechnician, day, toOccupy.get(0), tsDuration));
        }
        return freeTimeSlotDTOList;
    }

    /**
     * @param correctTSs         all correct beginSearchIndex time of TS
     * @param beginSearchIndex   the beginning index of the search in correctTSs
     * @param nbTimeSlotToOccupy the number of ts that the process must occupy
     *
     * @return all beginSearchIndex local time of each time slot that the process must take.
     */
    private List<LocalTime> getTimeSlotToOccupy(List<LocalTime> correctTSs, int beginSearchIndex, int nbTimeSlotToOccupy) {
        List<LocalTime> toOccupy = new ArrayList<>(correctTSs.subList(beginSearchIndex, beginSearchIndex + nbTimeSlotToOccupy));

        if (toOccupy.size() != nbTimeSlotToOccupy) {
            log.error("ToOccupy list size ({}) is not equal to nbTimeSlotToOccupy ({})", toOccupy.size(), nbTimeSlotToOccupy);
            throw new IllegalArgumentException("ToOccupy size not equal to nbTimeSlotToOccupy");
        }

        return toOccupy;
    }

    private boolean canOccupyAllTimeSlots(List<LocalTime> toOccupy, List<TimeSlot> existingTS) {
        boolean free = true;
        for (LocalTime lt : toOccupy) {
            if (!isFree(lt, existingTS)) {
                free = false;
                break;
            }
        }
        return free;
    }

    /**
     * Remove time slot already passed
     *
     * @param day                 the day
     * @param freeTimeSlotDTOList the list to filter
     *
     * @return the list of time slot which are not already passed from the current local time of the server
     */
    private List<FreeTimeSlotDTO> filterWithCurrentTime(LocalDate day, List<FreeTimeSlotDTO> freeTimeSlotDTOList) {
        return freeTimeSlotDTOList.stream().filter(freeTimeSlotDTO -> {
            LocalTime tsTime = LocalTime.parse(freeTimeSlotDTO.getBegin());
            LocalDateTime tsDateTime = LocalDateTime.of(day, tsTime);

            LocalDateTime currentDateTime = LocalDateTime.now();

            return tsDateTime.isAfter(currentDateTime);
        }).toList();
    }

    private boolean isFree(LocalTime time, List<TimeSlot> existingTS) {
        for (TimeSlot timeSlot : existingTS)
            if (!timeSlot.isFree() && timeSlot.getBegin().equals(time))
                return false;

        return true;
    }

    private FreeTimeSlotDTO buildFreeTimeSlot(int idTechnician, LocalDate day, LocalTime tsBeginTime, int tsNumberMinutes) {
        return FreeTimeSlotDTO.builder()
                .idTechnician(idTechnician)
                .day(day.toString())
                .begin(tsBeginTime.toString())
                .duration(tsNumberMinutes)
                .build();
    }

    public static class TechnicianException extends RuntimeException {
        public TechnicianException(String message) {
            super(message);
        }
    }

}
