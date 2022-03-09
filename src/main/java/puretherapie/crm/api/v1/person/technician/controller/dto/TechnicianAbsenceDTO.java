package puretherapie.crm.api.v1.person.technician.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianAbsenceDTO {

    private int idTechnicianAbsence;
    private String day;
    private String beginTime;
    private String endTime;
    private TechnicianDTO technician;

}
