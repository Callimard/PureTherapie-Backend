package puretherapie.crm.data.notification;

import lombok.*;
import puretherapie.crm.api.v1.notification.controller.dto.NotificationDTO;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "creationDate", nullable = false)
    private LocalDateTime creationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idNotificationLevel", nullable = false)
    private NotificationLevel notificationLevel;

    public NotificationDTO transform() {
        return NotificationDTO.builder()
                .idNotification(idNotification)
                .type(type)
                .notificationTitle(notificationTitle)
                .text(text)
                .creationDate(creationDate != null ? creationDate.toString() : null)
                .build();
    }
}