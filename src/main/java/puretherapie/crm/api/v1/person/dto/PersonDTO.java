package puretherapie.crm.api.v1.person.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {

    protected int idPerson;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected boolean gender;
    protected String birthday;
    protected String phone;
    protected int idPersonOrigin;

}
