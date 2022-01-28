package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.appointment.controller.dto.AppointmentDTO;
import puretherapie.crm.api.v1.person.technician.dto.TechnicianDTO;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {

    private int idTimeSlot;
    private String day;
    private String begin;
    private int time;
    private boolean free;
    private TechnicianDTO technician;
    private AppointmentDTO appointment;

}
