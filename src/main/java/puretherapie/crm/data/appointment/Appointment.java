package puretherapie.crm.data.appointment;

import lombok.*;
import puretherapie.crm.api.v1.appointment.controller.dto.AppointmentDTO;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
    private Integer idAppointment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAestheticCare", nullable = false)
    private AestheticCare aestheticCare;

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
    private boolean canceled;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    @Column(name = "finalized", nullable = false)
    private boolean finalized;

    @OneToMany(targetEntity = TimeSlot.class, mappedBy = "appointment")
    @ToString.Exclude
    private List<TimeSlot> timeSlots;

    public AppointmentDTO transform() {
        return AppointmentDTO.builder()
                .idAppointment(idAppointment)
                .aestheticCare(aestheticCare != null ? aestheticCare.transform() : null)
                .client(client != null ? client.transform() : null)
                .technician(technician != null ? technician.transform() : null)
                .clientArrival(clientArrival != null ? clientArrival.transform() : null)
                .canceled(canceled)
                .day(day != null ? day.toString() : null)
                .time(time != null ? time.toString() : null)
                .finalized(finalized)
                .timeSlots(timeSlots != null ? timeSlots.stream().map(TimeSlot::transformWithoutAppointment).toList() : null)
                .build();
    }

}