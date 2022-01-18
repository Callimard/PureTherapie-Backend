package puretherapie.crm.api.v1.appointment.controller.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TakeAppointmentDTO {

    private int idClient;
    private int idTechnician;
    private int idAestheticCare;
    private LocalDate day;
    private LocalTime beginTime;
    private boolean overlapAuthorized;

}
