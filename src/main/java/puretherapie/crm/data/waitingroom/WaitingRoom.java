package puretherapie.crm.data.waitingroom;

import lombok.*;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.appointment.Appointment;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "WaitingRoom")
public class WaitingRoom {
    @Id
    @Column(name = "idWaitingRoom", nullable = false)
    private Integer id;

    @Column(name = "arrivalDate", nullable = false)
    private OffsetDateTime arrivalDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAppointment", nullable = false)
    private Appointment appointment;
}