package puretherapie.crm.api.v1.appointment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.appointment.controller.dto.TakeAppointmentDTO;
import puretherapie.crm.api.v1.appointment.service.TakeAppointmentService;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.appointment.controller.AppointmentController.APPOINTMENT_URL;
import static puretherapie.crm.api.v1.appointment.service.TakeAppointmentService.APPOINTMENT_CREATION_SUCCESS;
import static puretherapie.crm.data.person.user.Role.BOSS_ROLE;
import static puretherapie.crm.data.person.user.Role.SECRETARY_ROLE;
import static puretherapie.crm.tool.ControllerTool.ERROR_FIELD;
import static puretherapie.crm.tool.ControllerTool.SUCCESS_FIELD;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(APPOINTMENT_URL)
public class AppointmentController {

    // Constants.

    public static final String APPOINTMENT_URL = API_V1_URL + "/appointments";

    public static final String NOTIFICATION_SUR_BOOKING_TITLE = "Sur booking fait lors de la prise d'un rendez-vous";
    public static final String NOTIFICATION_SUR_BOOKING_TEXT = "Sur booking de %s minutes pour le rendez-vous du client %s avec le technicien %s " +
            "le %s à %s";

    // Variables.

    private final TakeAppointmentService takeAppointmentService;
    private final NotificationCreationService notificationCreationService;
    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;

    // Methods.

    @PostMapping
    public ResponseEntity<Map<String, Object>> takeAnAppointment(@RequestBody TakeAppointmentDTO aInfo,
                                                                 Authentication authentication) {
        Map<String, Object> res;
        if (canAuthorizeOverlap(authentication)) {
            log.debug("Authorize to had overlap between appointment");
            res = takeAppointmentService.takeAppointment(aInfo.getIdClient(), aInfo.getIdTechnician(),
                                                         aInfo.getIdAestheticCare(),
                                                         aInfo.getDay(),
                                                         aInfo.getBeginTime(),
                                                         aInfo.isOverlapAuthorized());
        } else {
            log.debug("Not authorize to had overlap between appointment");
            res = takeAppointmentService.takeAppointment(aInfo.getIdClient(), aInfo.getIdTechnician(), aInfo.getIdAestheticCare(),
                                                         aInfo.getDay(),
                                                         aInfo.getBeginTime());
        }

        return generateTakeAnAppointmentResponse(aInfo, res);
    }

    private ResponseEntity<Map<String, Object>> generateTakeAnAppointmentResponse(TakeAppointmentDTO aInfo, Map<String, Object> res) {
        Map<String, Object> resp = new HashMap<>();
        if (res.containsKey(APPOINTMENT_CREATION_SUCCESS)) {
            resp.put(SUCCESS_FIELD, "Success to create appointment for the day %s at %s".formatted(aInfo.getDay(), aInfo.getBeginTime()));
            return ResponseEntity.ok(resp);
        } else {
            resp.put(ERROR_FIELD, "Fail to create appointment for the day %s at %s".formatted(aInfo.getDay(), aInfo.getBeginTime()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
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
