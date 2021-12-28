package puretherapie.crm.person;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Person")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "personType")
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPerson", nullable = false)
    private Long idPerson;

    private String firstName;

    private String lastName;

    private String mail;

    private boolean gender;

    private Date birthday;

    private String phone;

    private OffsetDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "idPersonOrigin")
    private PersonOrigin personOrigin;
}
