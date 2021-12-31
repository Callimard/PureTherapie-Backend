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
    private Integer id;

    @Column(name = "type", nullable = false)
    private Integer type;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "level", nullable = false)
    private Integer level;
}