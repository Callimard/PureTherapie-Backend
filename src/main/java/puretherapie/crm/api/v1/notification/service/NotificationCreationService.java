package puretherapie.crm.api.v1.notification.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.data.notification.Notification;
import puretherapie.crm.data.notification.NotificationLevel;
import puretherapie.crm.data.notification.NotificationView;
import puretherapie.crm.data.notification.repository.NotificationLevelRepository;
import puretherapie.crm.data.notification.repository.NotificationRepository;
import puretherapie.crm.data.notification.repository.NotificationViewRepository;
import puretherapie.crm.data.person.user.Role;
import puretherapie.crm.data.person.user.User;
import puretherapie.crm.data.person.user.repository.RoleRepository;
import puretherapie.crm.data.person.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class NotificationCreationService {

    // Variables.

    private final NotificationRepository notificationRepository;
    private final NotificationLevelRepository notificationLevelRepository;
    private final NotificationViewRepository notificationViewRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createNotification(String title, String text, String levelName, boolean isAnAlert) {
        if (unCorrectArgs(title, text, levelName)) return false;

        NotificationLevel level = notificationLevelRepository.findByNotificationLevelName(levelName);
        if (level == null) {
            log.error("Level not found for the level name {}", levelName);
            return false;
        }

        return createNotification(title, text, level, isAnAlert);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createNotification(String title, String text, NotificationLevel notificationLevel, boolean isAnAlert) {
        if (notificationLevel == null)
            notificationLevel = notificationLevelRepository.getAllRolesLevel();

        if (unCorrectArgs(title, text, notificationLevel.getNotificationLevelName())) return false;

        try {
            Notification notification = buildAndSaveNotification(title, text, notificationLevel, isAnAlert);

            List<Role> roles = findRoles(notificationLevel);
            if (roles != null && !roles.isEmpty()) {
                createNotificationView(notification, searchUserFromRole(roles));
                return true;
            } else {
                throw new NoRolesFoundException("No roles find for this notification level %s".formatted(notificationLevel));
            }
        } catch (Exception e) {
            log.error("Fail notification creation. ROLLBACK DONE. Error msg = {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private List<Role> findRoles(NotificationLevel notificationLevel) {
        List<Role> roles = roleRepository.findByNotificationLevels(notificationLevel);
        log.debug("For level {} find roles {}", notificationLevel, roles);
        return roles;
    }

    private Notification buildAndSaveNotification(String notificationTitle, String text, NotificationLevel notificationLevel, boolean isAnAlert) {
        Notification notification = notificationRepository.save(buildNotification(notificationTitle, text, notificationLevel, isAnAlert));
        log.info("Create Notification {}", notification);
        return notification;
    }

    private boolean unCorrectArgs(String notificationTitle, String text, String levelName) {
        if ((notificationTitle == null || notificationTitle.isBlank()) || (text == null || text.isBlank()) ||
                (levelName == null || levelName.isBlank())) {
            log.error("Notification title, text or level name must not be null or blank");
            return true;
        }
        return false;
    }

    private Notification buildNotification(String notificationTitle, String text, NotificationLevel notificationLevel, boolean isAnAlert) {
        return Notification.builder()
                .notificationTitle(notificationTitle)
                .text(text)
                .type(isAnAlert)
                .notificationLevel(notificationLevel)
                .creationDate(LocalDateTime.now())
                .build();
    }

    private Iterable<User> searchUserFromRole(List<Role> roles) {
        Set<User> users = new HashSet<>();
        roles.forEach(r -> users.addAll(userRepository.findByRoles(r)));
        return users;
    }

    private NotificationView buildNotificationView(Notification notification, User user) {
        return NotificationView.builder()
                .viewed(false)
                .notification(notification)
                .user(user)
                .build();
    }

    private void createNotificationView(Notification notification, Iterable<User> users) {
        for (User user : users) {
            NotificationView notificationView = notificationViewRepository.save(buildNotificationView(notification, user));
            log.info("Save NotificationView {}", notificationView);
        }
    }

    private static class NoRolesFoundException extends RuntimeException {
        public NoRolesFoundException(String msg) {
            super(msg);
        }
    }

}
