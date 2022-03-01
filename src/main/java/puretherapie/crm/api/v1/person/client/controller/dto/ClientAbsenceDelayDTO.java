package puretherapie.crm.api.v1.person.client.controller.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientAbsenceDelayDTO {

    private int nbAbsence;
    private int nbDelay;

}
