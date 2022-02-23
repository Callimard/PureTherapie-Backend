package puretherapie.crm.api.v1.notification.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.notification.NotificationView;
import puretherapie.crm.data.notification.repository.NotificationViewRepository;

@Slf4j
@AllArgsConstructor
@Service
public class NotificationService {

    // Constants.

    public static final String NOTIFICATION_VIEW_NOT_FOUND_ERROR = "notification_view_not_found_error";

    // Variables.

    private final NotificationViewRepository notificationViewRepository;

    // Methods.

    public void setNotificationViewed(int idNotificationView) {
        NotificationView notificationView = verifyNotificationView(idNotificationView);
        notificationView.setViewed(true);
        notificationViewRepository.save(notificationView);
    }

    private NotificationView verifyNotificationView(int idNotificationView) {
        NotificationView notificationView = notificationViewRepository.findByIdNotificationView(idNotificationView);
        if (notificationView == null)
            throw new NotificationServiceException(NOTIFICATION_VIEW_NOT_FOUND_ERROR);

        return notificationView;
    }

    // Exception.

    public static class NotificationServiceException extends RuntimeException {
        public NotificationServiceException(String message) {
            super(message);
        }
    }

}
