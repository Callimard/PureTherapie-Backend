package puretherapie.crm.data.notification;

import lombok.*;
import puretherapie.crm.data.person.user.User;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "NotificationView")
public class NotificationView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotificationView", nullable = false)
    private Integer id;

    @Column(name = "viewed", nullable = false)
    private Integer viewed;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idNotification", nullable = false)
    private Notification notification;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idUser", nullable = false)
    private User user;
}