package puretherapie.crm.api.v1.person.client.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.appointment.controller.dto.AppointmentDTO;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientBasicAppointmentDTO {

    private AppointmentDTO firstAppointment;
    private AppointmentDTO lastAppointment;

}
