package puretherapie.crm.api.v1.notification.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.notification.controller.dto.NotificationViewDTO;
import puretherapie.crm.api.v1.notification.service.NotificationService;
import puretherapie.crm.data.notification.NotificationView;
import puretherapie.crm.data.notification.repository.NotificationViewRepository;
import puretherapie.crm.data.person.user.User;

import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.notification.controller.NotificationController.NOTIFICATIONS_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(NOTIFICATIONS_URL)
public class NotificationController {

    // Constants.

    public static final String NOTIFICATIONS_URL = API_V1_URL + "/notifications";

    public static final String SET_NOTIFICATION_VIEWED = "/setViewed";

    // Variables.

    private NotificationService notificationService;
    private NotificationViewRepository notificationViewRepository;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping
    public List<NotificationViewDTO> getNotificationViews(@RequestParam(name = "filter", required = false, defaultValue = "0") int filter,
                                                          Authentication authentication) {
        if (authentication.getPrincipal() instanceof User user) {
            List<NotificationView> notificationViews;
            if (filter == 0) {
                notificationViews = notificationViewRepository.findByUser(user);
            } else if (filter == 1) {
                notificationViews = notificationViewRepository.findByUserAndViewed(user, true);
            } else {
                notificationViews = notificationViewRepository.findByUserAndViewed(user, false);
            }
            return notificationViews.stream().map(NotificationView::transform).toList();
        } else {
            throw new IllegalArgumentException("The principal is not a User instance");
        }
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PutMapping("/{idNotificationView}" + SET_NOTIFICATION_VIEWED)
    public void setNotificationViewed(@PathVariable(name = "idNotificationView") int idNotificationView) {
        notificationService.setNotificationViewed(idNotificationView);
    }

}
