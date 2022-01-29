package puretherapie.crm.api.v1.appointment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentDTO;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentResponseDTO;
import puretherapie.crm.api.v1.appointment.service.CancelAppointmentService;
import puretherapie.crm.api.v1.appointment.service.TakeAppointmentService;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.appointment.controller.AppointmentController.APPOINTMENT_URL;
import static puretherapie.crm.data.person.user.Role.BOSS_ROLE;
import static puretherapie.crm.data.person.user.Role.SECRETARY_ROLE;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(APPOINTMENT_URL)
public class AppointmentController {

    // Constants.

    public static final String APPOINTMENTS = "/appointments";
    public static final String APPOINTMENT_URL = API_V1_URL + APPOINTMENTS;

    public static final String APPOINTMENT_CANCELLATION = "/cancel";
    public static final String APPOINTMENT_CANCELLATION_URL = API_V1_URL + APPOINTMENT_CANCELLATION;

    public static final String NOTIFICATION_SUR_BOOKING_TITLE = "Sur booking fait lors de la prise d'un rendez-vous";
    public static final String NOTIFICATION_SUR_BOOKING_TEXT = "Sur booking de %s minutes pour le rendez-vous du client %s avec le technicien %s " +
            "le %s à %s";

    // Variables.

    private final TakeAppointmentService takeAppointmentService;
    private final CancelAppointmentService cancelAppointmentService;
    private final NotificationCreationService notificationCreationService;
    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping(APPOINTMENT_CANCELLATION)
    public ResponseEntity<SimpleResponseDTO> cancelAppointment(@RequestParam("idAppointment") int idAppointment) {
        return SimpleResponseDTO.generateResponse(cancelAppointmentService.cancelAppointment(idAppointment));
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PostMapping
    public ResponseEntity<TakeAppointmentResponseDTO> takeAnAppointment(@RequestBody TakeAppointmentDTO aInfo, Authentication authentication) {
        TakeAppointmentResponseDTO responseDTO;
        if (canAuthorizeOverlap(authentication)) {
            log.debug("Authorize to had overlap between appointment");
            responseDTO = takeAppointmentService.takeAppointment(aInfo.getIdClient(), aInfo.getIdTechnician(),
                                                                 aInfo.getIdAestheticCare(),
                                                                 LocalDate.parse(aInfo.getDay()),
                                                                 LocalTime.parse(aInfo.getBeginTime()),
                                                                 aInfo.isOverlapAuthorized());
        } else {
            log.debug("Not authorize to had overlap between appointment");
            responseDTO = takeAppointmentService.takeAppointment(aInfo.getIdClient(),
                                                                 aInfo.getIdTechnician(),
                                                                 aInfo.getIdAestheticCare(),
                                                                 LocalDate.parse(aInfo.getDay()),
                                                                 LocalTime.parse(aInfo.getBeginTime()));
        }

        if (responseDTO.isFailedResponse())
            return ResponseEntity.badRequest().body(responseDTO);
        else
            return ResponseEntity.ok(responseDTO);
    }

    private boolean canAuthorizeOverlap(Authentication auth) {
        if (auth == null) {
            log.info("Cannot authorize overlap if no authenticated");
            return false;
        } else {
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                List<String> role = authorities.stream().map(GrantedAuthority::getAuthority).toList();
                return role.contains(BOSS_ROLE) || role.contains(SECRETARY_ROLE);
            } else {
                log.info("Cannot authorize overlap if no BOSS_ROLE or SECRETARY_ROLE");
                return false;
            }
        }
    }
}
