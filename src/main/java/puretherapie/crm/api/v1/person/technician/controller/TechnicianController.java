package puretherapie.crm.api.v1.person.technician.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.person.technician.dto.TechnicianDTO;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.util.ArrayList;
import java.util.List;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.person.technician.controller.TechnicianController.TECHNICIANS_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(TECHNICIANS_URL)
public class TechnicianController {

    // Constants.

    public static final String TECHNICIANS_URL = API_V1_URL + "/technicians";

    // Variables.

    private TechnicianRepository technicianRepository;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @GetMapping
    public List<TechnicianDTO> getAllTechnicians(
            @RequestParam(name = "alsoInactivated", required = false, defaultValue = "false") boolean alsoInactivated) {
        List<Technician> technicians = searchAllTechnicians(alsoInactivated);

        List<TechnicianDTO> allTechnicians = new ArrayList<>();
        technicians.forEach(tech -> allTechnicians.add(tech.transform()));

        if (allTechnicians.isEmpty())
            log.error("Empty Technicians list");

        return allTechnicians;
    }

    private List<Technician> searchAllTechnicians(boolean alsoInactivated) {
        List<Technician> technicians;
        if (alsoInactivated)
            technicians = this.technicianRepository.findAll();
        else
            technicians = this.technicianRepository.findByIsActive(true);
        return technicians;
    }

}
