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
public class ExceptionalOpening implements Opening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idExceptionalOpening", nullable = false)
    private Integer idExceptionalOpening;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "openingTime", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closeTime", nullable = false)
    private LocalTime closeTime;

    @Override
    public LocalTime openingTime() {
        return openingTime;
    }

    @Override
    public LocalTime closeTime() {
        return closeTime;
    }
}