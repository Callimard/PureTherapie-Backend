package puretherapie.crm.data.appointment;

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
@Table(name = "ClientDelay")
public class ClientDelay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClientDelay", nullable = false)
    private Integer idClientDelay;

    @Column(name = "delayTime", nullable = false)
    private Integer delayTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAppointment", nullable = false)
    private Appointment appointment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;
}