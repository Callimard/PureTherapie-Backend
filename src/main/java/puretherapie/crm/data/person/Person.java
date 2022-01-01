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

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPerson", nullable = false)
    private Long idPerson;

    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Column(name = "mail", nullable = false)
    private String mail;

    @Column(name = "gender", nullable = false)
    private boolean gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "creationDate", nullable = false)
    private OffsetDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "idPersonOrigin", nullable = false)
    private PersonOrigin personOrigin;
}
