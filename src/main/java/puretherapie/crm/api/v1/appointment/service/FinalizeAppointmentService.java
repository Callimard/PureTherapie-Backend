package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.product.bill.service.PaymentService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;

@Slf4j
@AllArgsConstructor
@Service
public class FinalizeAppointmentService {

    // Constants.

    public static final String APPOINTMENT_NOT_FOUND_ERROR = "appointment_not_found_error";
    public static final String APPOINTMENT_CANCELED_ERROR = "appointment_canceled_error";
    public static final String APPOINTMENT_ALREADY_FINALIZED_ERROR = "appointment_already_finalized_error";
    public static final String CLIENT_HAS_NOT_PAID_TODAY_ERROR = "client_has_not_paid_today_error";

    // Variables.

    private final AppointmentRepository appointmentRepository;
    private final PaymentService paymentService;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRED)
    public SimpleResponseDTO finalizeAppointment(int idAppointment) {
        try {
            Appointment appointment = verifyAppointment(idAppointment);
            verifyAppointmentNotCanceled(appointment);
            verifyAppointmentNotFinalized(appointment);
            verifyClientHasDoneAtLeastOnePaymentToday(appointment.getClient());
            updateAppointment(appointment);
            return SimpleResponseDTO.generateSuccess("Success to finalize appointment");
        } catch (Exception e) {
            log.debug("Fail to finalize appointment, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private Appointment verifyAppointment(int idAppointment) {
        Appointment appointment = appointmentRepository.findByIdAppointment(idAppointment);

        if (appointment == null)
            throw new FinalizeAppointmentException(APPOINTMENT_NOT_FOUND_ERROR);

        return appointment;
    }

    private void verifyAppointmentNotCanceled(Appointment appointment) {
        if (appointment.isCanceled())
            throw new FinalizeAppointmentException(APPOINTMENT_CANCELED_ERROR);
    }

    private void verifyAppointmentNotFinalized(Appointment appointment) {
        if (appointment.isFinalized())
            throw new FinalizeAppointmentException(APPOINTMENT_ALREADY_FINALIZED_ERROR);
    }

    private void verifyClientHasDoneAtLeastOnePaymentToday(Client client) {
        if (paymentService.hasRemainingPayment(client) && !paymentService.hasDonePaymentToday(client)) {
                throw new FinalizeAppointmentException(CLIENT_HAS_NOT_PAID_TODAY_ERROR);
        }
    }

    private void updateAppointment(Appointment appointment) {
        appointment.setFinalized(true);
        appointment = appointmentRepository.save(appointment);
        log.info("Update appointment and set it finalized, appointment : {}", appointment);
    }

    // Exceptions.

    private static class FinalizeAppointmentException extends RuntimeException {
        public FinalizeAppointmentException(String message) {
            super(message);
        }
    }

}
