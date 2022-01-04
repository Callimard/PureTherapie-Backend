package puretherapie.crm.data.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.notification.NotificationLevel;

import static puretherapie.crm.data.notification.NotificationLevel.ALL_ROLES_LEVEL;

@Repository
public interface NotificationLevelRepository extends JpaRepository<NotificationLevel, Integer> {

    NotificationLevel findByIdNotificationLevel(Integer idNotificationLevel);

    NotificationLevel findByNotificationLevelName(String levelName);

    default NotificationLevel getAllRolesLevel() {
        return findByNotificationLevelName(ALL_ROLES_LEVEL);
    }
}