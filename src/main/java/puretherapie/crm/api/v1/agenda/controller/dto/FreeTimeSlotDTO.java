package puretherapie.crm.api.v1.agenda.controller.dto;

import lombok.*;
import puretherapie.crm.data.agenda.TimeSlot;

/**
 * Represent a time slot which is free. A time slot is free because there is no time slot in db at its time or there is a time slot with {@link
 * TimeSlot#isFree()} = true.
 */
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FreeTimeSlotDTO {
    private int idTechnician;
    private String day;
    private String begin;
    private int duration;
}
