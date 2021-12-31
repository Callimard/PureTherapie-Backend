package puretherapie.crm.data.note;

import lombok.*;
import puretherapie.crm.data.person.client.Client;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AssociationClientNote")
public class ClientNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAssociationClientNote", nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idNote", nullable = false)
    private Note note;
}