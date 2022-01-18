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
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.time.LocalDate;
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

    private final TechnicianRepository technicianRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotAtomService tsaService;
    private final OpeningService openingService;

    // Methods.

    public List<FreeTimeSlotDTO> getTechnicianFreeTimeSlot(int idTechnician, LocalDate day) {
        Technician technician = verifyTechnician(idTechnician);
        verifyDay(day);

        List<Opening> openingList = openingService.getOpenings(day);

        if (openingService.isOpen(day)) {
            List<TimeSlot> existingTS = timeSlotRepository.findByTechnicianAndDay(technician, day);

            List<FreeTimeSlotDTO> freeTimeSlotDTOs = new ArrayList<>();
            for (Opening opening : openingList)
                freeTimeSlotDTOs.addAll(searchFreeTS(opening, tsaService.searchCorrectTSA(day).getNumberOfMinutes(), existingTS, idTechnician, day));

            if (freeTimeSlotDTOs.isEmpty())
                log.info("No free Time Slot found for the technician {} at the day {}", technician.simplyIdentifier(), day);

            return freeTimeSlotDTOs;
        } else {
            log.info("No time slot free for technician {} at the day {}, because closed", technician.simplyIdentifier(), day);
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

    private Collection<FreeTimeSlotDTO> searchFreeTS(Opening opening, int tsDuration, List<TimeSlot> existingTS, int idTechnician,
                                                     LocalDate day) {
        List<FreeTimeSlotDTO> freeTimeSlotDTOList = new ArrayList<>();

        for (LocalTime tsBeginTime : correctTimeSlotTime(opening, tsDuration))
            if (isFree(tsBeginTime, existingTS))
                freeTimeSlotDTOList.add(buildFreeTimeSlot(idTechnician, day, tsBeginTime, tsDuration));

        return freeTimeSlotDTOList;
    }

    private boolean isFree(LocalTime tsBeginTime, List<TimeSlot> existingTS) {
        for (TimeSlot timeSlot : existingTS)
            if (!timeSlot.isFree() && timeSlot.getBegin().equals(tsBeginTime))
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

}
