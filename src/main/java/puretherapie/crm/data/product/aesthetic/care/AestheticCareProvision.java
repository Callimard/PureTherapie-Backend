package puretherapie.crm.data.product.aesthetic.care;

import lombok.*;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareProvisionDTO;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.technician.Technician;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private LocalDateTime date;

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

    public AestheticCareProvisionDTO transform() {
        return AestheticCareProvisionDTO.builder()
                .idAestheticCareProvision(idAestheticCareProvision)
                .date(date != null ? date.toString() : null)
                .client(client != null ? client.transform() : null)
                .appointment(appointment != null ? appointment.transform() : null)
                .technician(technician != null ? technician.transform() : null)
                .aestheticCare(aestheticCare != null ? aestheticCare.transform() : null)
                .build();
    }
}