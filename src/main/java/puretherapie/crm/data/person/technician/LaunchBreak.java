package puretherapie.crm.data.person.technician;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LaunchBreak")
public class LaunchBreak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLaunchBreak", nullable = false)
    private Integer id;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "beginHour", nullable = false)
    private LocalTime beginHour;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTechnician", nullable = false)
    private Technician technician;
}