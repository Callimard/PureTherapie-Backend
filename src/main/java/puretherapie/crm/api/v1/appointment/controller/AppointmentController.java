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
import puretherapie.crm.api.v1.appointment.AppointmentInformation;
import puretherapie.crm.api.v1.appointment.service.AppointmentCreationService;
import puretherapie.crm.api.v1.notification.service.NotificationCreationService;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.appointment.controller.AppointmentController.API_V1_APPOINTMENT_URL;
import static puretherapie.crm.data.notification.NotificationLevel.BOSS_SECRETARY_LEVEL;
import static puretherapie.crm.data.person.user.Role.BOSS_ROLE;
import static puretherapie.crm.data.person.user.Role.SECRETARY_ROLE;
import static puretherapie.crm.tool.ControllerTool.ERROR_FIELD;
import static puretherapie.crm.tool.ControllerTool.SUCCESS_FIELD;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(API_V1_APPOINTMENT_URL)
public class AppointmentController {

    // Constants.

    public static final String API_V1_APPOINTMENT_URL = API_V1_URL + "/appointments";

    public static final String NOTIFICATION_SUR_BOOKING_TITLE = "Sur booking fait lors de la prise d'un rendez-vous";
    public static final String NOTIFICATION_SUR_BOOKING_TEXT = "Sur booking de %s minutes pour le rendez-vous du client %s avec le technicien %s " +
            "le%s à %s";

    // Variables.

    private final AppointmentCreationService appointmentCreationService;
    private final NotificationCreationService notificationCreationService;
    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;

    // Methods.

    @PostMapping
    public ResponseEntity<Map<String, Object>> takeAnAppointment(@RequestBody AppointmentInformation aInfo,
                                                                 Authentication authentication) {
        boolean success;
        boolean potentialSurBooking = false;
        if (canHadOverlap(authentication)) {
            log.debug("Authorize to had overlap");
            success = appointmentCreationService.createAppointment(aInfo.getIdClient(), aInfo.getIdTechnician(), aInfo.getIdAestheticCare(),
                                                                   aInfo.getDay(),
                                                                   aInfo.getBeginTime(),
                                                                   aInfo.getOverlapAuthorized());
            potentialSurBooking = true;
        } else {
            log.debug("Not authorize to had overlap");
            success = appointmentCreationService.createAppointment(aInfo.getIdClient(), aInfo.getIdTechnician(), aInfo.getIdAestheticCare(),
                                                                   aInfo.getDay(),
                                                                   aInfo.getBeginTime());
        }

        return generateTakeAnAppointmentResponse(aInfo, success, potentialSurBooking);
    }

    private ResponseEntity<Map<String, Object>> generateTakeAnAppointmentResponse(AppointmentInformation aInfo, boolean success,
                                                                                  boolean potentialSurBooking) {
        Map<String, Object> resp = new HashMap<>();
        if (success) {
            if (surBookingHasBeenDone(aInfo, potentialSurBooking))
                createSurBookingNotification(aInfo);

            resp.put(SUCCESS_FIELD, "Fail to create appointment");
            return ResponseEntity.ok(resp);
        } else {
            resp.put(ERROR_FIELD, "Fail to create appointment");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    private boolean surBookingHasBeenDone(@RequestBody AppointmentInformation aInfo, boolean potentialSurBooking) {
        return aInfo.getOverlapAuthorized() > 0 && potentialSurBooking;
    }

    private void createSurBookingNotification(AppointmentInformation aInfo) {
        Client c = clientRepository.findByIdPerson(aInfo.getIdClient());
        Technician t = technicianRepository.findByIdPerson(aInfo.getIdTechnician());
        notificationCreationService.createNotification(NOTIFICATION_SUR_BOOKING_TITLE,
                                                       NOTIFICATION_SUR_BOOKING_TEXT.formatted(aInfo.getOverlapAuthorized(),
                                                                                               c.simplyIdentifier(),
                                                                                               t.simplyIdentifier(), aInfo.getDay(),
                                                                                               aInfo.getBeginTime()), BOSS_SECRETARY_LEVEL,
                                                       true);
    }

    private boolean canHadOverlap(Authentication auth) {
        if (auth == null)
            return false;
        else {
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                List<String> role = authorities.stream().map(GrantedAuthority::getAuthority).toList();
                return role.contains(BOSS_ROLE) || role.contains(SECRETARY_ROLE);
            } else
                return false;
        }
    }
}
