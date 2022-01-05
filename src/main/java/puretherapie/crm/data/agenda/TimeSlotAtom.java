package puretherapie.crm.data.agenda;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TimeSlotAtom")
public class TimeSlotAtom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTimeSlotAtom", nullable = false)
    private Integer idTimeSlotAtom;

    @Column(name = "numberOfMinutes", nullable = false)
    private Integer numberOfMinutes;

    @Column(name = "effectiveDate", nullable = false)
    private LocalDate effectiveDate;
}