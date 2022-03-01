package puretherapie.crm.api.v1.person.technician.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.person.dto.PersonDTO;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianDTO extends PersonDTO {

    private String speciality;
    private boolean active;

    @Builder
    public TechnicianDTO(int idPerson, String firstName, String lastName, String email, boolean gender, String birthday, String phone,
                         int idPersonOrigin, String speciality, boolean active) {
        super(idPerson, firstName, lastName, email, gender, birthday, phone, idPersonOrigin);
        this.speciality = speciality;
        this.active = active;
    }
}
