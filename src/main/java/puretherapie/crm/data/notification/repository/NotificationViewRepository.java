package puretherapie.crm.data.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.notification.NotificationView;

@Repository
public interface NotificationViewRepository extends JpaRepository<NotificationView, Integer> {
}