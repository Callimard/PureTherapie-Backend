package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GlobalOpeningTimeDTO {

    private int idGlobalOpeningTime;
    private int day;
    private String openingTime;
    private String closeTime;

}
