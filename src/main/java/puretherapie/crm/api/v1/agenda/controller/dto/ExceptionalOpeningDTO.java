package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;
import puretherapie.crm.data.agenda.ExceptionalOpening;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionalOpeningDTO {

    private int idExceptionalOpening;
    private String day;
    private String openingTime;
    private String closeTime;

    public ExceptionalOpening transform() {
        return ExceptionalOpening.builder()
                .idExceptionalOpening(idExceptionalOpening)
                .day(day != null && !day.isBlank() ? LocalDate.parse(day) : null)
                .openingTime(openingTime != null && !openingTime.isBlank() ? LocalTime.parse(openingTime) : null)
                .closeTime(closeTime != null && !closeTime.isBlank() ? LocalTime.parse(closeTime) : null)
                .build();
    }
}
