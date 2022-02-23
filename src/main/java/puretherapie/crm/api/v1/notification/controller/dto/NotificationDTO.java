package puretherapie.crm.api.v1.notification.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private int idNotification;
    private boolean type;
    private String notificationTitle;
    private String text;
    private String creationDate;

}
