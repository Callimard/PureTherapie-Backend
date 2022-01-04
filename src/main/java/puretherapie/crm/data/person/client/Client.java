package puretherapie.crm.data.person.client;

import lombok.*;
import puretherapie.crm.api.v1.client.ClientInformation;
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

    // Fields.

    public static final String PHOTO_FIELD = "photo";
    public static final String COMMENT_FIELD = "comment";
    public static final String TECHNICAL_COMMENT_FIELD = "technicalComment";

    // Variables.

    @Column(name = PHOTO_FIELD)
    private String photo;

    @Column(name = COMMENT_FIELD)
    private String comment;

    @Column(name = TECHNICAL_COMMENT_FIELD)
    private String technicalComment;

    @Builder
    public Client(Integer idPerson, String firstName, String lastName, String email, boolean gender, LocalDate birthday, String phone,
                  OffsetDateTime creationDate, PersonOrigin personOrigin, String photo, String comment, String technicalComment) {
        super(idPerson, firstName, lastName, email, gender, birthday, phone, creationDate, personOrigin);
        this.photo = photo;
        this.comment = comment;
        this.technicalComment = technicalComment;
    }

    public ClientInformation getClientInformation() {
        return ClientInformation.builder()
                .photo(getPhoto())
                .comment(getComment())
                .technicalComment(getTechnicalComment())
                .firstName(getFirstName())
                .lastName(getLastName())
                .email(getEmail())
                .gender(isGender())
                .birthday(getBirthday())
                .phone(getPhone())
                .idOrigin(getPersonOrigin().getIdPersonOrigin())
                .build();
    }
}
