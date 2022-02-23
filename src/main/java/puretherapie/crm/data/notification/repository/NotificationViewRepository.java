package puretherapie.crm.data.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.notification.NotificationView;
import puretherapie.crm.data.person.user.User;

import java.util.List;

@Repository
public interface NotificationViewRepository extends JpaRepository<NotificationView, Integer> {

    NotificationView findByIdNotificationView(int idNotificationView);

    List<NotificationView> findByUser(User user);

    List<NotificationView> findByUserAndViewed(User user, boolean viewed);
}