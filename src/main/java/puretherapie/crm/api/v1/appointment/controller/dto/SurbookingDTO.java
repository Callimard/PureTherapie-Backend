package puretherapie.crm.api.v1.appointment.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareDTO;
import puretherapie.crm.data.appointment.Surbooking;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SurbookingDTO {

    private int idSurbooking;
    private ClientDTO client;
    private AestheticCareDTO aestheticCare;
    private ClientArrivalDTO clientArrival;
    private boolean canceled;
    private String day;
    private String time;
    protected boolean finalized;

    public Surbooking transform() {
        return Surbooking.builder()
                .idSurbooking(idSurbooking)
                .client(client != null ? client.transform() : null)
                .aestheticCare(aestheticCare != null ? aestheticCare.transform() : null)
                .clientArrival(clientArrival != null ? clientArrival.transform() : null)
                .canceled(canceled)
                .day(day != null && !day.isBlank() ? LocalDate.parse(day) : null)
                .time(time != null && !time.isBlank() ? LocalTime.parse(time) : null)
                .finalized(finalized)
                .build();
    }

}
