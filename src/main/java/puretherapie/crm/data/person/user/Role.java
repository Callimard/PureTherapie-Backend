package puretherapie.crm.data.person.user;

import lombok.*;
import puretherapie.crm.data.notification.NotificationLevel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Role")
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRole", nullable = false)
    private Integer id;

    @Column(name = "roleName", nullable = false, length = 15)
    private String roleName;

    @ManyToMany
    @JoinTable(name = "AssociationRoleNotificationLevel",
            joinColumns = @JoinColumn(name = "idRole"),
            inverseJoinColumns = @JoinColumn(name = "idNotificationLevel"))
    @ToString.Exclude
    private List<NotificationLevel> notificationLevels;

    @ManyToMany
    @JoinTable(name = "AssociationRoleAuthority",
            joinColumns = @JoinColumn(name = "idRole"),
            inverseJoinColumns = @JoinColumn(name = "idAuthority"))
    @ToString.Exclude
    private List<Authority> authorities;

    @ManyToMany
    @JoinTable(name = "AssociationUserRole",
            joinColumns = @JoinColumn(name = "idRole"),
            inverseJoinColumns = @JoinColumn(name = "idPerson"))
    @ToString.Exclude
    private List<User> users;
}