package puretherapie.crm.api.v1.waitingroom.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.appointment.controller.dto.AppointmentDTO;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRoomDTO {

    private int idWaitingRoom;
    private String arrivalDate;
    private String appointmentTime;
    private ClientDTO client;
    private AppointmentDTO appointment;

}
