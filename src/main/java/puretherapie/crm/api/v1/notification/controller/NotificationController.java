package puretherapie.crm.api.v1.notification.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.notification.NotificationInformation;
import puretherapie.crm.api.v1.notification.service.NotificationService;
import puretherapie.crm.data.notification.NotificationLevel;
import puretherapie.crm.data.notification.repository.NotificationLevelRepository;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.notification.controller.NotificationController.API_V1_NOTIFICATION_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(API_V1_NOTIFICATION_URL)
public class NotificationController {

    // Constants.

    public static final String API_V1_NOTIFICATION_URL = API_V1_URL + "/notifications";

    // Variables.

    private final NotificationLevelRepository notificationLevelRepository;
    private final NotificationService notificationService;

    // Methods.

    @PostMapping
    public ResponseEntity<String> postNotification(@RequestBody NotificationInformation notificationInformation) {
        NotificationLevel level = notificationLevelRepository.findByIdNotificationLevel(notificationInformation.getIdLevel());

        boolean success = notificationService.createNotification(notificationInformation.getTitle(), notificationInformation.getText(), level,
                                                                 notificationInformation.isAnAlert());

        log.error("Success to add notification = {}", success);

        return ResponseEntity.ok(String.valueOf(success));
    }

}
