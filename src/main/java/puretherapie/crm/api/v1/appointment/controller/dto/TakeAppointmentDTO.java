package puretherapie.crm.api.v1.appointment.controller.dto;

import lombok.*;

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
    private String day;
    private String beginTime;
    private boolean overlapAuthorized;

}
