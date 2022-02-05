package puretherapie.crm.api.v1.product.aesthetic.care.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.appointment.controller.dto.AppointmentDTO;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.person.technician.dto.TechnicianDTO;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AestheticCareProvisionDTO {

    private int idAestheticCareProvision;
    private String date;
    private ClientDTO client;
    private AppointmentDTO appointment;
    private TechnicianDTO technician;
    private AestheticCareDTO aestheticCare;

}
