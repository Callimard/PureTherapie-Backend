package puretherapie.crm.api.v1.person.technician.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.person.technician.controller.dto.TechnicianAbsenceDTO;
import puretherapie.crm.api.v1.person.technician.controller.dto.TechnicianDTO;
import puretherapie.crm.api.v1.person.technician.controller.parameter.TechnicianAbsenceCreationParameter;
import puretherapie.crm.api.v1.person.technician.service.TechnicianAbsenceService;
import puretherapie.crm.api.v1.person.technician.service.TechnicianService;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.TechnicianAbsence;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.person.technician.controller.TechnicianController.TECHNICIANS_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(TECHNICIANS_URL)
public class TechnicianController {

    // Constants.

    public static final String TECHNICIANS_URL = API_V1_URL + "/technicians";

    public static final String TECHNICIAN_ABSENCES = "/{idTechnician}/absences";
    public static final String TECHNICIAN_ABSENCES_URL = TECHNICIANS_URL + TECHNICIAN_ABSENCES;

    public static final String DELETE_ABSENCE = "/absences/{idTechnicianAbsence}";
    public static final String DELETE_ABSENCE_URL = TECHNICIANS_URL + DELETE_ABSENCE;

    // Variables.

    private TechnicianRepository technicianRepository;
    private TechnicianAbsenceService technicianAbsenceService;
    private TechnicianService technicianService;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @GetMapping(TECHNICIAN_ABSENCES)
    public List<TechnicianAbsenceDTO> getAllTechnicianAbsence(@PathVariable(name = "idTechnician") int idTechnician) {
        return technicianAbsenceService.getAllTechnicianAbsences(idTechnician).stream().map(TechnicianAbsence::transform).toList();
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @DeleteMapping("/{idTechnician}")
    public void inactivateTechnician(@PathVariable(name = "idTechnician") int idTechnician) {
        technicianService.inactivateTechnician(idTechnician);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping("/{idTechnician}/activate")
    public void activateTechnician(@PathVariable(name = "idTechnician") int idTechnician) {
        technicianService.activateTechnician(idTechnician);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping(TECHNICIAN_ABSENCES)
    public void createTechnicianAbsence(@PathVariable(name = "idTechnician") int idTechnician,
                                        @RequestBody TechnicianAbsenceCreationParameter parameter) {
        technicianAbsenceService.createTechnicianAbsence(idTechnician, LocalDate.parse(parameter.getDay()),
                                                         LocalTime.parse(parameter.getBeginTime()), LocalTime.parse(parameter.getEndTime()));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @DeleteMapping(DELETE_ABSENCE)
    public void deleteTechnicianAbsence(@PathVariable(name = "idTechnicianAbsence") int idTechnicianAbsence) {
        technicianAbsenceService.deleteTechnicianAbsence(idTechnicianAbsence);
    }

    @GetMapping
    public List<TechnicianDTO> getAllTechnicians(@RequestParam(name = "active", required = false, defaultValue = "true") boolean active) {
        return this.technicianRepository.findByActive(active).stream().map(Technician::transform).toList();
    }

}
