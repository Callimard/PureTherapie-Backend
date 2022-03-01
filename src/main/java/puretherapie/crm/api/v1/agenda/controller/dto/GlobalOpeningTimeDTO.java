package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;
import puretherapie.crm.data.agenda.GlobalOpeningTime;

import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GlobalOpeningTimeDTO {

    private int idGlobalOpeningTime;
    private int dayNumber;
    private String openingTime;
    private String closeTime;

    public GlobalOpeningTime transform() {
        return GlobalOpeningTime.builder()
                .idGlobalOpeningTime(idGlobalOpeningTime)
                .dayNumber(dayNumber)
                .openingTime(openingTime != null && !openingTime.isBlank() ? LocalTime.parse(openingTime) : null)
                .closeTime(closeTime != null && !closeTime.isBlank() ? LocalTime.parse(closeTime) : null)
                .build();
    }

}
