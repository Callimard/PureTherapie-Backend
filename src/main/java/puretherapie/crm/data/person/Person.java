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
@RequiredArgsConstructor
@Entity
@Table(name = "Person")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "personType")
public abstract class Person {

    // Constants.

    public static final String NO_PHONE = "0000000000";

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPerson", nullable = false)
    @NonNull
    private Long idPerson;

    @Column(name = "firstName", nullable = false)
    @NonNull
    private String firstName;

    @Column(name = "lastName", nullable = false)
    @NonNull
    private String lastName;

    @Column(name = "mail", nullable = false)
    @NonNull
    private String mail;

    @Column(name = "gender", nullable = false)
    @NonNull
    private boolean gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "phone", nullable = false)
    @NonNull
    private String phone;

    @Column(name = "creationDate", nullable = false)
    @NonNull
    private OffsetDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "idPersonOrigin", nullable = false)
    @NonNull
    private PersonOrigin personOrigin;
}
