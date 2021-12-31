package puretherapie.crm.data.person.client.appointment;

import lombok.*;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAppointment", nullable = false)
    private Integer id;

    @Column(name = "clientArrived")
    private Integer clientArrived;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAestheticCare", nullable = false)
    private AestheticCare aestheticCare;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTimeSlot", nullable = false)
    private TimeSlot timeSlot;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTechnician", nullable = false)
    private Technician technician;

    @ManyToOne
    @JoinColumn(name = "idClientArrival")
    private ClientArrival clientArrival;

    @Column(name = "canceled", nullable = false)
    private Boolean canceled = false;
}