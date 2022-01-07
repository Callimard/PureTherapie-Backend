package puretherapie.crm.data.waitingroom;

import lombok.*;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.client.Client;

import javax.persistence.*;
import java.time.LocalTime;
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

    @Column(name = "appointmentTime")
    private LocalTime appointmentTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAppointment")
    private Appointment appointment;
}