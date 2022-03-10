package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.historical.service.HistoricalCreationService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.api.v1.waitingroom.service.RemoveFromWaitingRoomService;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.technician.Technician;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static puretherapie.crm.data.historical.HistoricalLevel.BOSS_LEVEL;

@Slf4j
@AllArgsConstructor
@Service
public class CancelAppointmentService {

    // Constants.

    private static final String APPOINTMENT_CANCELED_TITLE = "Annulation RDV";
    private static final String APPOINTMENT_CANCELED_TEXT = "Le RDV du client %s le %s à %s avec le/la technicien(ne) %s a été annulé";

    public static final String CANCEL_APPOINTMENT_SUCCESS = "cancel_appointment_success";
    public static final String CANCEL_APPOINTMENT_FAIL = "cancel_appointment_fail";

    public static final String APPOINTMENT_NOT_FOUND_ERROR = "appointment_not_found_error";
    public static final String CLIENT_IS_ARRIVED_ERROR = "client_is_arrived_error";
    public static final String APPOINTMENT_FINALIZED_ERROR = "appointment_is_finalized_error";
    public static final String REMOVE_FROM_WR_ERROR = "remove_from_wr_error";

    // Variables.

    private final RemoveFromWaitingRoomService removeFromWaitingRoomService;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final HistoricalCreationService historicalCreationService;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRED)
    public SimpleResponseDTO cancelAppointment(int idAppointment) {
        try {
            Appointment appointment = verifyAppointment(idAppointment);
            verifyClientIsNotArrived(appointment);
            verifyAppointmentNotFinalized(appointment);
            if (!appointment.isCanceled()) {
                setAppointmentCanceled(appointment);
                updateAllAppointmentTimeSlots(appointment);
                removeClientFromWR(appointment);
                notifyAppointmentCanceled(appointment.getClient(), appointment.getDay(), appointment.getTime(), appointment.getTechnician());
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

    private void verifyClientIsNotArrived(Appointment appointment) {
        if (appointment.getClientArrival() != null)
            throw new CancelAppointmentException(CLIENT_IS_ARRIVED_ERROR);
    }

    private void verifyAppointmentNotFinalized(Appointment appointment) {
        if (appointment.isFinalized())
            throw new CancelAppointmentException(APPOINTMENT_FINALIZED_ERROR);
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

    private void removeClientFromWR(Appointment appointment) {
        SimpleResponseDTO res = removeFromWaitingRoomService.removeClient(appointment.getClient().getIdPerson());
        if (!res.success())
            throw new CancelAppointmentException(REMOVE_FROM_WR_ERROR);
    }

    private void notifyAppointmentCanceled(Client client, LocalDate day, LocalTime time, Technician technician) {
        boolean success = historicalCreationService.createHistorical(APPOINTMENT_CANCELED_TITLE,
                                                                     APPOINTMENT_CANCELED_TEXT.formatted(client.simplyIdentifier(), day, time,
                                                                                                             technician.simplyIdentifier()),
                                                                     BOSS_LEVEL, true);

        if (!success)
            log.error("Fail to create appointment cancellation notification");
    }

    // Exceptions.

    private static class CancelAppointmentException extends RuntimeException {
        public CancelAppointmentException(String message) {
            super(message);
        }
    }
}
