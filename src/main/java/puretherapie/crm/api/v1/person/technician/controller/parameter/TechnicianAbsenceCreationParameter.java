package puretherapie.crm.api.v1.person.technician.controller.parameter;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianAbsenceCreationParameter {

    private int idTechnician;
    private String day;
    private String beginTime;
    private String endTime;

}
