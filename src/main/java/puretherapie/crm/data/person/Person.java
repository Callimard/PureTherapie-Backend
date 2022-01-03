package puretherapie.crm.data.person;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Person")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "personType")
public abstract class Person {

    // Constants.

    public static final String NO_PHONE = "NO_PHONE";

    public static final int FIRST_NAME_MAX_LENGTH = 20;
    public static final int LAST_NAME_MAX_LENGTH = 30;
    public static final int MAIL_MAX_LENGTH = 255;
    public static final int PHONE_MAX_LENGTH = 20;

    // Fields.

    public static final String ID_PERSON_FIELD = "idPerson";
    public static final String FIRST_NAME_FIELD = "firstName";
    public static final String LAST_NAME_FIELD = "lastName";
    public static final String EMAIL_FIELD = "email";
    public static final String GENDER_FIELD = "gender";
    public static final String BIRTHDAY_FIELD = "birthday";
    public static final String PHONE_FIELD = "phone";
    public static final String CREATION_DATE_FIELD = "creationDate";
    public static final String ID_PERSON_ORIGIN_FIELD = "idPersonOrigin";

    // Constraints.

    public static final String UNIQUE_EMAIL_CONSTRAINTS = "Person.uq_email";
    public static final String UNIQUE_PHONE_CONSTRAINTS = "Person.uq_phone";

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_PERSON_FIELD, nullable = false)
    private Integer idPerson;

    @Column(name = FIRST_NAME_FIELD, nullable = false)
    private String firstName;

    @Column(name = LAST_NAME_FIELD, nullable = false)
    private String lastName;

    @Column(name = EMAIL_FIELD, nullable = false)
    private String email;

    @Column(name = GENDER_FIELD, nullable = false)
    private boolean gender;

    @Column(name = BIRTHDAY_FIELD)
    private LocalDate birthday;

    @Column(name = PHONE_FIELD, nullable = false)
    private String phone;

    @Column(name = CREATION_DATE_FIELD, nullable = false)
    private OffsetDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = ID_PERSON_ORIGIN_FIELD, nullable = false)
    private PersonOrigin personOrigin;
}
