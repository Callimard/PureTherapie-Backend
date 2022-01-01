package puretherapie.crm.data.person.client;

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
@Table(name = "Client")
@DiscriminatorValue("C")
public class Client extends Person {

    // Constants.

    public static final String NO_PHOTO = "NO_PHOTO";

    public static final int PHOTO_MAX_LENGTH = 255;
    public static final int COMMENT_MAX_LENGTH = 255;
    public static final int TECHNICAL_COMMENT_MAX_LENGTH = 255;

    // Variables.

    @Column(name = "photo")
    private String photo;

    @Column(name = "comment")
    private String comment;

    @Column(name = "technicalComment")
    private String technicalComment;

    @Builder
    public Client(Long idPerson, String firstName, String lastName, String mail, boolean gender, LocalDate birthday, String phone,
                  OffsetDateTime creationDate, PersonOrigin personOrigin, String photo, String comment, String technicalComment) {
        super(idPerson, firstName, lastName, mail, gender, birthday, phone, creationDate, personOrigin);
        this.photo = photo;
        this.comment = comment;
        this.technicalComment = technicalComment;
    }
}
