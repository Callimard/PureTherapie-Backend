package puretherapie.crm.data.person;

import lombok.*;
import puretherapie.crm.api.v1.user.controller.dto.PersonOriginDTO;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PersonOrigin")
public class PersonOrigin {

    // Constants.

    public static final String NONE_TYPE = "None";

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPersonOrigin", nullable = false)
    private Integer idPersonOrigin;

    @Column(name = "type", nullable = false)
    private String type;

    // Methods.

    public PersonOriginDTO buildDTO() {
        return PersonOriginDTO.builder().idPersonOrigin(idPersonOrigin).type(type).build();
    }
}
