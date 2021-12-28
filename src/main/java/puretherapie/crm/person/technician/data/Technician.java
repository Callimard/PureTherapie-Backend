package puretherapie.crm.person.technician.data;

import lombok.*;
import puretherapie.crm.person.Person;
import puretherapie.crm.person.PersonOrigin;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.sql.Date;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DiscriminatorValue("T")
public class Technician extends Person {

    private String speciality;

    @Builder
    public Technician(Long idPerson, String firstName, String lastName, String mail, boolean gender, Date birthday, String phone,
                      OffsetDateTime creationDate, PersonOrigin personOrigin, String speciality) {
        super(idPerson, firstName, lastName, mail, gender, birthday, phone, creationDate, personOrigin);
        this.speciality = speciality;
    }
}
