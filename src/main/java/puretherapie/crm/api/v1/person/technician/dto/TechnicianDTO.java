package puretherapie.crm.api.v1.person.technician.dto;

import lombok.*;
import puretherapie.crm.api.v1.person.dto.PersonDTO;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianDTO extends PersonDTO {

    private String speciality;
    private boolean isActive;

    @Builder
    public TechnicianDTO(int idPerson, String firstName, String lastName, String email, boolean gender, String birthday, String phone,
                         int idPersonOrigin, String speciality, boolean isActive) {
        super(idPerson, firstName, lastName, email, gender, birthday, phone, idPersonOrigin);
        this.speciality = speciality;
        this.isActive = isActive;
    }
}
