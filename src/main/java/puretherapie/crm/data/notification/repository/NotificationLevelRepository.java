package puretherapie.crm.data.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.notification.NotificationLevel;

@Repository
public interface NotificationLevelRepository extends JpaRepository<NotificationLevel, Integer> {

    NotificationLevel findByIdNotificationLevel(Integer idNotificationLevel);

}