package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.appointment.controller.dto.AppointmentDTO;
import puretherapie.crm.api.v1.person.technician.dto.TechnicianDTO;

import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO implements Comparable<TimeSlotDTO> {

    private int idTimeSlot;
    private String day;
    private String begin;
    private int time;
    private boolean free;
    private boolean isLaunchBreak;
    private boolean isAbsence;
    private TechnicianDTO technician;
    private AppointmentDTO appointment;

    @Override
    public int compareTo(TimeSlotDTO o) {
        return LocalTime.parse(this.begin).compareTo(LocalTime.parse(o.getBegin()));
    }
}
