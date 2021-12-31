package puretherapie.crm.data.agenda;

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
@Table(name = "ExceptionalOpening")
public class ExceptionalOpening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idExceptionalOpening", nullable = false)
    private Integer id;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "opentTime", nullable = false)
    private LocalTime opentTime;

    @Column(name = "closeTime", nullable = false)
    private LocalTime closeTime;
}