package puretherapie.crm.data.person.technician;

import lombok.*;
import puretherapie.crm.data.person.Person;
import puretherapie.crm.data.person.PersonOrigin;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Technician")
@DiscriminatorValue("T")
public class Technician extends Person {

    @Column(name = "speciality")
    private String speciality;

    @Builder
    public Technician(Long idPerson, String firstName, String lastName, String mail, boolean gender, LocalDate birthday, String phone,
                      OffsetDateTime creationDate, PersonOrigin personOrigin, String speciality) {
        super(idPerson, firstName, lastName, mail, gender, birthday, phone, creationDate, personOrigin);
        this.speciality = speciality;
    }
}
