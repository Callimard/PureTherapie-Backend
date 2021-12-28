package puretherapie.crm.person.client.data;

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
@DiscriminatorValue("C")
public class Client extends Person {

    private String photo;

    private String comment;

    private String technicalComment;

    @Builder
    public Client(Long idPerson, String firstName, String lastName, String mail, boolean gender, Date birthday, String phone,
                  OffsetDateTime creationDate, PersonOrigin personOrigin, String photo, String comment, String technicalComment) {
        super(idPerson, firstName, lastName, mail, gender, birthday, phone, creationDate, personOrigin);
        this.photo = photo;
        this.comment = comment;
        this.technicalComment = technicalComment;
    }
}
