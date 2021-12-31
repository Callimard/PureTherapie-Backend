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
@Table(name = "TechnicianAbsence")
public class TechnicianAbsence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTechnicianAbsence", nullable = false)
    private Integer id;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "beginTime", nullable = false)
    private LocalTime beginTime;

    @Column(name = "endTime", nullable = false)
    private LocalTime endTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTechnician", nullable = false)
    private Technician technician;
}