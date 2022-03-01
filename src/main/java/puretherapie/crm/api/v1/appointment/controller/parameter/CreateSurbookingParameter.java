package puretherapie.crm.api.v1.appointment.controller.parameter;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateSurbookingParameter {

    private int idClient;
    private int idAC;
    private String day;
    private String time;

}
