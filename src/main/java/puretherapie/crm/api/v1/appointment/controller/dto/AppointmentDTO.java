package puretherapie.crm.api.v1.appointment.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.agenda.controller.dto.TimeSlotDTO;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.person.technician.dto.TechnicianDTO;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareDTO;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    private int idAppointment;
    private AestheticCareDTO aestheticCare;
    private ClientDTO client;
    private TechnicianDTO technician;
    private ClientArrivalDTO clientArrival;
    private boolean canceled;
    private String day;
    private String time;
    private boolean finalized;
    private List<TimeSlotDTO> timeSlots;

}
