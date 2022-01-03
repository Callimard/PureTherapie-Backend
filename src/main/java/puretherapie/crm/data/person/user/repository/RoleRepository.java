package puretherapie.crm.data.person.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.notification.NotificationLevel;
import puretherapie.crm.data.person.user.Role;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    List<Role> findByNotificationLevels(NotificationLevel notificationLevel);

    Role findByRoleName(String name);

}