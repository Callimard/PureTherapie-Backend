package puretherapie.crm.api.v1.appointment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.appointment.controller.dto.SurbookingDTO;
import puretherapie.crm.api.v1.appointment.controller.parameter.CreateSurbookingParameter;
import puretherapie.crm.api.v1.appointment.service.SurbookingService;
import puretherapie.crm.data.appointment.Surbooking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.appointment.controller.SurbookingController.SURBOOKINGS_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(SURBOOKINGS_URL)
public class SurbookingController {

    // Constants.

    public static final String SURBOOKINGS = "/surbookings";
    public static final String SURBOOKINGS_URL = API_V1_URL + SURBOOKINGS;

    public static final String SURBOOKING_CLIENT_ARRIVE = "/{idSurbooking}/clientArrive";

    public static final String SURBOOKING_FINALIZE = "/{idSurbooking}/finalize";

    // Variables.

    private final SurbookingService surbookingService;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping
    public List<SurbookingDTO> getAllSurbooking(@RequestParam(name = "day") String day,
                                                @RequestParam(name = "canceled", required = false, defaultValue = "false") boolean canceled) {
        return surbookingService.getAllSurbooking(LocalDate.parse(day), canceled).stream().map(Surbooking::transform).toList();
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping
    public void createSurbooking(@RequestBody CreateSurbookingParameter parameter) {
        surbookingService.createSurbooking(parameter.getIdClient(), parameter.getIdAC(),
                                           LocalDate.parse(parameter.getDay()), LocalTime.parse(parameter.getTime()));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @DeleteMapping("/{idSurbooking}")
    public void canceledSurbooking(@PathVariable(name = "idSurbooking") int idSurbooking) {
        surbookingService.cancelSurbooking(idSurbooking);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping(SURBOOKING_CLIENT_ARRIVE)
    public void clientArrive(@PathVariable(name = "idSurbooking") int idSurbooking) {
        surbookingService.clientArrive(idSurbooking);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping(SURBOOKING_FINALIZE)
    public void finalizeSurbooking(@PathVariable(name = "idSurbooking") int idSurbooking) {
        surbookingService.finalizedSurbooking(idSurbooking);
    }

}
