package puretherapie.crm.data.notification;

import lombok.*;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotification", nullable = false)
    private Integer idNotification;

    @Column(name = "type", nullable = false)
    private Boolean type;

    @Column(name = "notificationTitle", nullable = false, length = 125)
    private String notificationTitle;

    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idNotificationLevel", nullable = false)
    private NotificationLevel notificationLevel;
}