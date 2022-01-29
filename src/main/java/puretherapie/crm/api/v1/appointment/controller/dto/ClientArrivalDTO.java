package puretherapie.crm.api.v1.appointment.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientArrivalDTO {

    private int idClientArrival;
    private String arrivalDate;
    private ClientDTO client;

}
