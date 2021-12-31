package puretherapie.crm.data.note;

import lombok.*;
import puretherapie.crm.data.person.client.appointment.Appointment;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AssociationAppointmentNote")
public class AppointmentNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAssociationAppointmentNote", nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAppointment", nullable = false)
    private Appointment appointment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idNote", nullable = false)
    private Note note;
}