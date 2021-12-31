package puretherapie.crm.data.agenda;

import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GlobalOpeningTime")
public class GlobalOpeningTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idGlobalOpeningTime", nullable = false)
    private Integer id;

    @Column(name = "day", nullable = false)
    private Integer day;

    @Column(name = "opentTime", nullable = false)
    private LocalTime opentTime;

    @Column(name = "closeTime", nullable = false)
    private LocalTime closeTime;
}