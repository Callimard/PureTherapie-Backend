package puretherapie.crm.api.v1.notification.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.data.notification.Notification;
import puretherapie.crm.data.notification.NotificationLevel;
import puretherapie.crm.data.notification.NotificationView;
import puretherapie.crm.data.notification.repository.NotificationRepository;
import puretherapie.crm.data.notification.repository.NotificationViewRepository;
import puretherapie.crm.data.person.user.Role;
import puretherapie.crm.data.person.user.User;
import puretherapie.crm.data.person.user.repository.RoleRepository;
import puretherapie.crm.data.person.user.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class NotificationService {

    // Variables.

    private final NotificationRepository notificationRepository;
    private final NotificationViewRepository notificationViewRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean createNotification(String notificationTitle, String text, NotificationLevel notificationLevel, boolean isAnAlert) {
        if (notificationLevel == null || notificationLevel.getNotificationLevelName().isBlank())
            return false;

        Notification notification = notificationRepository.save(buildNotification(notificationTitle, text, notificationLevel, isAnAlert));
        log.info("Create Notification {}", notification);

        List<Role> roles = roleRepository.findByNotificationLevels(notificationLevel);
        if (roles != null && !roles.isEmpty()) {
            Set<User> users = searchUserFromRole(roles);
            createNotificationView(notification, users);
            return true;
        } else {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private Notification buildNotification(String notificationTitle, String text, NotificationLevel notificationLevel, boolean isAnAlert) {
        return Notification.builder()
                .notificationTitle(notificationTitle)
                .text(text)
                .type(isAnAlert)
                .notificationLevel(notificationLevel)
                .build();
    }

    private Set<User> searchUserFromRole(List<Role> roles) {
        Set<User> users = new HashSet<>();
        roles.forEach(r -> users.addAll(userRepository.findByRoles(r)));
        log.debug("For roles: {} find users: {}", roles, users);
        return users;
    }

    private NotificationView buildNotificationView(Notification notification, User user) {
        return NotificationView.builder()
                .viewed(false)
                .notification(notification)
                .user(user)
                .build();
    }

    private void createNotificationView(Notification notification, Set<User> users) {
        for (User user : users) {
            NotificationView notificationView = notificationViewRepository.save(buildNotificationView(notification, user));
            log.info("Create NotificationView {}", notificationView);
        }
    }


}
