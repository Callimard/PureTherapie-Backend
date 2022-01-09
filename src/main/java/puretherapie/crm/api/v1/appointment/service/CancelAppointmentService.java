package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class CancelAppointmentService extends SimpleService {

    // Constants.

    public static final String CANCEL_APPOINTMENT_SUCCESS = "cancel_appointment_success";
    public static final String CANCEL_APPOINTMENT_FAIL = "cancel_appointment_fail";

    public static final String APPOINTMENT_NOT_FOUND_ERROR = "appointment_not_found_error";

    // Variables.

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> cancelAppointment(int idAppointment) {
        try {
            Appointment appointment = verifyAppointment(idAppointment);
            if (!appointment.isCanceled()) {
                updateAppointment(appointment);
                updateAllAppointmentTimeSlots(appointment);
            } else
                log.debug("Already canceled appointment, nothing has been done");

            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to create cancel appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private Appointment verifyAppointment(int idAppointment) {
        Appointment appointment = appointmentRepository.findByIdAppointment(idAppointment);
        if (appointment == null)
            throw new CancelAppointmentException("Not found appointment", generateError(APPOINTMENT_NOT_FOUND_ERROR, "Appointment not found"));

        return appointment;
    }

    private void updateAppointment(Appointment appointment) {
        appointment.setCanceled(true);
        appointment = appointmentRepository.save(appointment);
        log.debug("Update appointment (set it to canceled = true) -> {}", appointment);
    }

    private void updateAllAppointmentTimeSlots(Appointment appointment) {
        List<TimeSlot> appointmentTimeSlots = timeSlotRepository.findByAppointment(appointment);
        for (TimeSlot timeSlot : appointmentTimeSlots)
            timeSlot.setFree(true);
        timeSlotRepository.saveAll(appointmentTimeSlots);
        log.debug("Update all time slots -> set them to free = true");
    }

    // SimpleService methods.

    @Override
    public String getSuccessTag() {
        return CANCEL_APPOINTMENT_SUCCESS;
    }

    @Override
    public String getFailTag() {
        return CANCEL_APPOINTMENT_FAIL;
    }

    // Exceptions.

    private static class CancelAppointmentException extends SimpleService.ServiceException {
        public CancelAppointmentException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }
}
