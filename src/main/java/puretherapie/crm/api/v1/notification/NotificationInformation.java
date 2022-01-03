package puretherapie.crm.api.v1.notification;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInformation {
    private String title;
    private String text;
    private boolean isAnAlert;
    private int idLevel;
}
