package puretherapie.crm.data.product.aesthetic.care;

import lombok.*;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.technician.Technician;

import javax.persistence.*;
import java.time.OffsetDateTime;

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
    private Integer idAestheticCareProvision;

    @Column(name = "date", nullable = false)
    private OffsetDateTime date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "idAppointment")
    private Appointment appointment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTechnician", nullable = false)
    private Technician technician;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAestheticCare", nullable = false)
    private AestheticCare aestheticCare;
}