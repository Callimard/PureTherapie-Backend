package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;

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

}
