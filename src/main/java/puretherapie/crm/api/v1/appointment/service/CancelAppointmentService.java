package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class CancelAppointmentService {

    // Constants.

    public static final String CANCEL_APPOINTMENT_SUCCESS = "cancel_appointment_success";
    public static final String CANCEL_APPOINTMENT_FAIL = "cancel_appointment_fail";

    public static final String APPOINTMENT_NOT_FOUND_ERROR = "appointment_not_found_error";

    // Variables.

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRED)
    public SimpleResponseDTO cancelAppointment(int idAppointment) {
        try {
            Appointment appointment = verifyAppointment(idAppointment);
            if (!appointment.isCanceled()) {
                setAppointmentCanceled(appointment);
                updateAllAppointmentTimeSlots(appointment);
            } else
                log.debug("Already canceled appointment, nothing has been done");

            return SimpleResponseDTO.generateSuccess("Success to cancel appointment");
        } catch (Exception e) {
            log.debug("Fail to create cancel appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private Appointment verifyAppointment(int idAppointment) {
        Appointment appointment = appointmentRepository.findByIdAppointment(idAppointment);
        if (appointment == null)
            throw new CancelAppointmentException(APPOINTMENT_NOT_FOUND_ERROR);

        return appointment;
    }

    private void setAppointmentCanceled(Appointment appointment) {
        appointment.setCanceled(true);
        appointment = appointmentRepository.save(appointment);
        log.debug("Update appointment (set it to canceled = true) -> {}", appointment);
    }

    /**
     * Free all timeslots associate to the appointment.
     *
     * @param appointment the appointment to cancel
     */
    private void updateAllAppointmentTimeSlots(Appointment appointment) {
        List<TimeSlot> appointmentTimeSlots = timeSlotRepository.findByAppointment(appointment);
        for (TimeSlot timeSlot : appointmentTimeSlots)
            timeSlot.setFree(true);
        timeSlotRepository.saveAll(appointmentTimeSlots);
        log.debug("Update all time slots -> set them to free = true");
    }

    // Exceptions.

    private static class CancelAppointmentException extends RuntimeException {
        public CancelAppointmentException(String message) {
            super(message);
        }
    }
}
