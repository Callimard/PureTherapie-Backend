package puretherapie.crm.person;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PersonOrigin")
public class PersonOrigin {

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPersonOrigin", nullable = false)
    private Long idPersonOrigin;

    private String type;
}
