package puretherapie.crm.data.waitingroom;

import lombok.*;
import puretherapie.crm.api.v1.waitingroom.controller.dto.WaitingRoomDTO;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.client.Client;

import javax.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idWaitingRoom", nullable = false)
    private Integer idWaitingRoom;

    @Column(name = "arrivalDate", nullable = false)
    private LocalDateTime arrivalDate;

    @Column(name = "appointmentTime")
    private LocalTime appointmentTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne()
    @JoinColumn(name = "idAppointment")
    private Appointment appointment;

    public WaitingRoomDTO transform() {
        return WaitingRoomDTO.builder()
                .idWaitingRoom(idWaitingRoom)
                .arrivalDate(arrivalDate != null ? arrivalDate.toString() : null)
                .appointmentTime(appointmentTime != null ? appointmentTime.toString() : null)
                .client(client != null ? client.transform() : null)
                .appointment(appointment != null ? appointment.transform() : null)
                .build();
    }
}