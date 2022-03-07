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
    private int idTimeSlot;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "begin", nullable = false)
    private LocalTime begin;

    /**
     * Duration, in number of minutes.
     */
    @Column(name = "duration", nullable = false)
    private int duration;

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
                .duration(duration)
                .free(free)
                .isLaunchBreak(false)
                .isAbsence(false)
                .technician(technician != null ? technician.transform() : null)
                .appointment(appointment != null ? appointment.transform() : null)
                .build();
    }

    public TimeSlotDTO transformWithoutAppointment() {
        return TimeSlotDTO.builder()
                .idTimeSlot(idTimeSlot)
                .day(day != null ? day.toString() : null)
                .begin(begin != null ? begin.toString() : null)
                .duration(duration)
                .free(free)
                .isLaunchBreak(false)
                .isAbsence(false)
                .technician(technician != null ? technician.transform() : null)
                .appointment(null)
                .build();
    }

}