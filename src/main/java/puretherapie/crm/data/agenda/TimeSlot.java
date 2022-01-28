package puretherapie.crm.data.agenda;

import lombok.*;
import puretherapie.crm.api.v1.agenda.controller.dto.TimeSlotDTO;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.technician.Technician;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TimeSlot")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTimeSlot", nullable = false)
    private Integer idTimeSlot;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "begin", nullable = false)
    private LocalTime begin;

    @Column(name = "time", nullable = false)
    private Integer time;

    @Column(name = "free", nullable = false)
    private boolean free;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idTechnician", nullable = false)
    private Technician technician;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAppointment", nullable = false)
    private Appointment appointment;

    public TimeSlotDTO transform() {
        return TimeSlotDTO.builder()
                .idTimeSlot(idTimeSlot)
                .day(day != null ? day.toString() : null)
                .begin(begin != null ? begin.toString() : null)
                .time(time)
                .free(free)
                .technician(technician != null ? technician.transform() : null)
                .appointment(appointment != null ? appointment.transform() : null)
                .build();
    }

}