package puretherapie.crm.data.agenda;

import lombok.*;
import puretherapie.crm.data.person.technician.Technician;

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
@Table(name = "TimeSlot")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTimeSlot", nullable = false)
    private Integer idTimeSlot;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "begin", nullable = false)
    private LocalTime begin;

    @Column(name = "time", nullable = false)
    private Integer time;

    @Column(name = "free", nullable = false)
    private boolean free = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTechnician", nullable = false)
    private Technician technician;
}