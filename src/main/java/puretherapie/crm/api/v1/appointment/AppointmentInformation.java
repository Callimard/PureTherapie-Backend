package puretherapie.crm.api.v1.appointment;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentInformation {

    private int idClient;
    private int idTechnician;
    private int idAestheticCare;
    private LocalDate day;
    private LocalTime time;
    private int overlapAuthorized;

}
