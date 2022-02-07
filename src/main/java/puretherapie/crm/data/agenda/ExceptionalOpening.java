package puretherapie.crm.data.agenda;

import lombok.*;
import puretherapie.crm.api.v1.agenda.controller.dto.ExceptionalOpeningDTO;

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

    public ExceptionalOpeningDTO transform() {
        return ExceptionalOpeningDTO.builder()
                .idExceptionalOpening(idExceptionalOpening)
                .day(day != null ? day.toString() : null)
                .openingTime(openingTime != null ? openingTime.toString() : null)
                .closeTime(closeTime != null ? closeTime.toString() : null)
                .build();
    }
}