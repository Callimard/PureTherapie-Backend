package puretherapie.crm.data.appointment;

import lombok.*;
import puretherapie.crm.api.v1.appointment.controller.dto.ClientArrivalDTO;
import puretherapie.crm.data.person.client.Client;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private LocalDateTime arrivalDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    public ClientArrivalDTO transform() {
        return ClientArrivalDTO.builder()
                .idClientArrival(idClientArrival)
                .arrivalDate(arrivalDate != null ? arrivalDate.toString() : null)
                .client(client != null ? client.transform() : null)
                .build();
    }
}