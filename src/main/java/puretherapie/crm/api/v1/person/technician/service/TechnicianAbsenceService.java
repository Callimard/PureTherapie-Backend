package puretherapie.crm.api.v1.person.technician.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.agenda.controller.dto.TimeSlotDTO;
import puretherapie.crm.api.v1.agenda.service.OpeningService;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.TechnicianAbsence;
import puretherapie.crm.data.person.technician.repository.TechnicianAbsenceRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static puretherapie.crm.tool.TimeTool.isInTZ;

@Slf4j
@AllArgsConstructor
@Service
public class TechnicianAbsenceService {

    // Variables.

    private final TechnicianAbsenceRepository technicianAbsenceRepository;
    private final TechnicianRepository technicianRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final OpeningService openingService;

    // Methods.

    public List<TechnicianAbsence> getAllTechnicianAbsences(int idTechnician) {
        Technician technician = technicianRepository.findByIdPerson(idTechnician);
        return technicianAbsenceRepository.findByTechnician(technician);
    }

    public void createTechnicianAbsence(int idTechnician, LocalDate day, LocalTime beginTime, LocalTime endTime) {
        Technician technician = technicianRepository.findByIdPerson(idTechnician);

        if (!beginTime.isBefore(endTime))
            throw new TechnicianAbsenceException("Technician absence begin time not before end time");

        saveTechnicianAbsence(buildTechnicianAbsence(day, beginTime, endTime, technician));
    }

    private TechnicianAbsence buildTechnicianAbsence(LocalDate day, LocalTime beginTime, LocalTime endTime, Technician technician) {
        return TechnicianAbsence.builder()
                .technician(technician)
                .day(day)
                .beginTime(beginTime)
                .endTime(endTime)
                .build();
    }

    private void saveTechnicianAbsence(TechnicianAbsence technicianAbsence) {
        technicianAbsence = technicianAbsenceRepository.save(technicianAbsence);
        log.info("Save new TechnicianAbsence => {}", technicianAbsence);
    }

    public void deleteTechnicianAbsence(int idTechnicianAbsence) {
        technicianAbsenceRepository.deleteById(idTechnicianAbsence);
    }

    /**
     * Verify if the TS is in a TechnicianAbsence or not
     *
     * @param technician the technician
     * @param day        the day of the ts
     * @param beginTime  the ts begin time
     * @param duration   the ts duration
     *
     * @return true if the ts is in a technician absence, else false.
     */
    public boolean isInTechnicianAbsence(Technician technician, LocalDate day, LocalTime beginTime, int duration) {
        List<TechnicianAbsence> technicianAbsences = technicianAbsenceRepository.findByTechnicianAndDay(technician, day);
        return isInTechnicianAbsence(technicianAbsences, beginTime, duration);
    }

    public boolean isInTechnicianAbsence(List<TechnicianAbsence> technicianAbsences, LocalTime beginTime, int duration) {
        if (!technicianAbsences.isEmpty()) {
            for (TechnicianAbsence technicianAbsence : technicianAbsences)
                if (isInTechnicianAbsence(technicianAbsence, beginTime, duration))
                    return true;
        }
        return false;
    }

    public boolean isInTechnicianAbsence(TechnicianAbsence technicianAbsence, LocalTime beginTime, int duration) {
        return isInTZ(technicianAbsence.getBeginTime(), technicianAbsence.getEndTime(), beginTime, duration);
    }

    // Exception.

    public static class TechnicianAbsenceException extends RuntimeException {
        public TechnicianAbsenceException(String message) {
            super(message);
        }
    }

}
