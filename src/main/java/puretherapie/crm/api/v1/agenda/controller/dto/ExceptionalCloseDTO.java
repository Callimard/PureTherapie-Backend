package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;
import puretherapie.crm.data.agenda.ExceptionalClose;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionalCloseDTO {

    private int idExceptionalClose;
    private String day;

    public ExceptionalClose transform() {
        return ExceptionalClose.builder()
                .idExceptionalClose(idExceptionalClose)
                .day(day != null && !day.isBlank() ? LocalDate.parse(day) : null)
                .build();
    }

}
