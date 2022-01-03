package puretherapie.crm.data.notification;

import lombok.*;
import puretherapie.crm.data.person.user.Role;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NotificationLevel")
public class NotificationLevel implements Serializable {

    // Constants.

    public static final String ALL_ROLES_LEVEL = "ALL_ROLES";
    public static final String BOSS_SECRETARY_LEVEL = "BOSS_SECRETARY";
    public static final String BOSS_SECRETARY_MAMY_LEVEL = "BOSS_SECRETARY_MAMY";
    public static final String BOSS_SECRETARY_MAMY_TECHNICIAN_LEVEL = "BOSS_SECRETARY_MAMY_TECHNICIAN";

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotificationLevel", nullable = false)
    private Integer idNotificationLevel;

    @Column(name = "notificationLevelName", nullable = false, length = 45)
    private String notificationLevelName;

    @ManyToMany
    @JoinTable(name = "AssociationRoleNotificationLevel",
            joinColumns = @JoinColumn(name = "idNotificationLevel"),
            inverseJoinColumns = @JoinColumn(name = "idRole"))
    @ToString.Exclude
    private List<Role> roles;
}