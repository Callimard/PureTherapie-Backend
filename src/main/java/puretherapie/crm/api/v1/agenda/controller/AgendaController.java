package puretherapie.crm.api.v1.agenda.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.agenda.controller.dto.FreeTimeSlotDTO;
import puretherapie.crm.api.v1.person.technician.service.TechnicianService;

import java.time.LocalDate;
import java.util.List;

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

    // Variables.

    private final TechnicianService technicianService;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "false")
    @GetMapping(TECHNICIAN_FREE_TIME_SLOTS)
    public List<FreeTimeSlotDTO> getTechnicianFreeTimeSlots(@RequestParam(name = "idTechnician") int idTechnician, @RequestParam(name = "day")
            LocalDate day) {
        return technicianService.getTechnicianFreeTimeSlot(idTechnician, day);
    }

}
