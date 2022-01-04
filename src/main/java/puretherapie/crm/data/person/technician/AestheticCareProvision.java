package puretherapie.crm.data.person.technician;

import lombok.*;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.appointment.Appointment;

import javax.persistence.*;
import java.time.Instant;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AestheticCareProvision")
public class AestheticCareProvision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAestheticCareProvision", nullable = false)
    private Integer id;

    @Column(name = "date", nullable = false)
    private Instant date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client idClient;

    @ManyToOne
    @JoinColumn(name = "idAppointment")
    private Appointment idAppointment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTechnician", nullable = false)
    private Technician idTechnician;
}