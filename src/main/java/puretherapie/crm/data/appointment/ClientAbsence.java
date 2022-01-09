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
@Table(name = "ClientAbsence")
public class ClientAbsence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClientAbsence", nullable = false)
    private Integer idClientAbsence;

    @Column(name = "level", nullable = false)
    private Integer level;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAppointment", nullable = false)
    private Appointment appointment;
}