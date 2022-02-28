package puretherapie.crm.api.v1.appointment.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.data.appointment.ClientArrival;

import java.time.LocalDateTime;

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

    public ClientArrival transform() {
        return ClientArrival.builder()
                .idClientArrival(idClientArrival)
                .arrivalDate(arrivalDate != null && !arrivalDate.isBlank() ? LocalDateTime.parse(arrivalDate) : null)
                .client(client != null ? client.transform() : null)
                .build();
    }

}
