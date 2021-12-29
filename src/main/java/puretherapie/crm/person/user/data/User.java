package puretherapie.crm.person.user.data;

import lombok.*;
import puretherapie.crm.person.Person;
import puretherapie.crm.person.PersonOrigin;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "User")
@DiscriminatorValue("U")
public class User extends Person {

    private String username;

    private String password;

    @Builder
    public User(Long idPerson, String firstName, String lastName, String mail, boolean gender, Date birthday, String phone,
                OffsetDateTime creationDate, PersonOrigin personOrigin, String username, String password) {
        super(idPerson, firstName, lastName, mail, gender, birthday, phone, creationDate, personOrigin);
        this.username = username;
        this.password = password;
    }
}
