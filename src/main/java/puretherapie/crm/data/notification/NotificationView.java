package puretherapie.crm.data.notification;

import lombok.*;
import puretherapie.crm.api.v1.notification.controller.dto.NotificationViewDTO;
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
    private Integer idNotificationView;

    @Column(name = "viewed", nullable = false)
    private Boolean viewed;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idNotification", nullable = false)
    private Notification notification;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idUser", nullable = false)
    private User user;

    public NotificationViewDTO transform() {
        return NotificationViewDTO.builder()
                .idNotificationView(idNotificationView)
                .viewed(viewed)
                .notification(notification != null ? notification.transform() : null)
                .build();
    }
}