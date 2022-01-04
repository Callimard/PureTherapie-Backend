package puretherapie.crm.api.v1.appointment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.appointment.AppointmentInformation;
import puretherapie.crm.api.v1.appointment.service.AppointmentCreationService;

import java.security.Principal;
import java.util.Map;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.appointment.controller.AppointmentController.API_V1_APPOINTMENT_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(API_V1_APPOINTMENT_URL)
public class AppointmentController {

    public static final String API_V1_APPOINTMENT_URL = API_V1_URL + "/appointments";

    private final AppointmentCreationService appointmentCreationService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> takeAnAppointment(@RequestBody AppointmentInformation appointmentInformation,
                                                                 Authentication authentication, Principal principal) {
        log.debug("Authentication username {} password {}", authentication.getName(), authentication.getCredentials());

        log.debug("Principal = {}, class = {}", authentication.getPrincipal(), authentication.getPrincipal().getClass());

        return ResponseEntity.ok(null);
    }
}
