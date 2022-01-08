package puretherapie.crm.data.appointment;

import lombok.*;
import puretherapie.crm.data.person.client.Client;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ClientArrival")
public class ClientArrival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClientArrival", nullable = false)
    private Integer idClientArrival;

    @Column(name = "arrivalDate", nullable = false)
    private OffsetDateTime arrivalDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;
}