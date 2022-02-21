package puretherapie.crm.api.v1.notification.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NotificationViewDTO {

    private int idNotificationView;
    private boolean viewed;
    private NotificationDTO notification;

}
