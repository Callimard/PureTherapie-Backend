package puretherapie.crm.api.v1.waitingroom.service;

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
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;

import java.time.LocalDateTime;
import java.util.List;

import static puretherapie.crm.tool.TimeTool.today;

@Slf4j
@AllArgsConstructor
@Service
public class PlaceInWaitingRoomService {

    // ERRORS.

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found";
    public static final String NON_COHERENCE_BETWEEN_CLIENT_APPOINTMENT_ERROR = "non_coherence_between_client_appointment";
    public static final String APPOINTMENT_CANCELED_ERROR = "appointment_canceled";
    public static final String TIME_SLOT_INCOHERENCE_ERROR = "time_slot_incoherence";
    public static final String APPOINTMENT_NOT_FOR_TODAY_ERROR = "appointment_not_for_today";

    // Variables.

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WaitingRoomRepository waitingRoomRepository;

    // Methods.

    @Transactional(propagation = Propagation.SUPPORTS)
    public SimpleResponseDTO placeClient(Client client, Appointment appointment) {
        return placeClient(client.getIdPerson(), appointment != null ? appointment.getIdAppointment() : -1);
    }

    /**
     * Client with no appointment
     *
     * @param idClient the sur booking client
     *
     * @return the result of the try to place client in waiting room
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public SimpleResponseDTO placeClient(int idClient) {
        return placeClient(idClient, -1);
    }

    /**
     * Try to add client in the waiting room. If the id appointment exists, the appointment must be for today and must be not canceled.
     *
     * @param idClient      id client
     * @param idAppointment id appointment (can be a not found appointment)
     *
     * @return the result of the try to place client in waiting room
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public SimpleResponseDTO placeClient(int idClient, int idAppointment) {
        try {
            Client client = verifyClient(idClient);
            Appointment appointment = getAppointment(idAppointment);
            verifyClientAndAppointmentCoherence(client, appointment);
            verifyAppointmentIsForToday(appointment);
            saveWaitingRoom(client, appointment);
            return SimpleResponseDTO.generateSuccess("Success to place client in Waiting room");
        } catch (Exception e) {
            log.debug("Fail to place client in waiting room", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private Client verifyClient(int idClient) {
        Client c = clientRepository.findByIdPerson(idClient);
        if (c == null)
            throw new PlaceClientInWaitingRoomException(CLIENT_NOT_FOUND_ERROR);
        return c;
    }

    private Appointment getAppointment(int idAppointment) {
        return appointmentRepository.findByIdAppointment(idAppointment);
    }

    private void verifyClientAndAppointmentCoherence(Client client, Appointment appointment) {
        if (appointment == null) {
            log.debug("Appointment null -> sur booking client");
            return;
        }

        if (!appointment.getClient().getIdPerson().equals(client.getIdPerson()))
            throw new PlaceClientInWaitingRoomException(NON_COHERENCE_BETWEEN_CLIENT_APPOINTMENT_ERROR);

        if (appointment.isCanceled())
            throw new PlaceClientInWaitingRoomException(APPOINTMENT_CANCELED_ERROR);
    }

    private void verifyAppointmentIsForToday(Appointment appointment) {
        if (appointment != null) {
            List<TimeSlot> timeSlots = timeSlotRepository.findByAppointmentOrderByBeginAsc(appointment);
            verifyTimeSlot(timeSlots);
            TimeSlot first = timeSlots.get(0);
            if (!first.getDay().equals(today()))
                throw new PlaceClientInWaitingRoomException(APPOINTMENT_NOT_FOR_TODAY_ERROR);
        }
    }

    private void verifyTimeSlot(List<TimeSlot> timeSlots) {
        for (TimeSlot timeSlot : timeSlots) {
            if (timeSlot == null || timeSlot.isFree())
                throw new PlaceClientInWaitingRoomException(TIME_SLOT_INCOHERENCE_ERROR);
        }
    }

    private void saveWaitingRoom(Client client, Appointment appointment) {
        WaitingRoom waitingRoom = buildWaitingRoom(client, appointment);
        waitingRoom = waitingRoomRepository.save(waitingRoom);
        log.debug("Save waiting room {}", waitingRoom);
    }

    private WaitingRoom buildWaitingRoom(Client client, Appointment appointment) {
        TimeSlot firstTS = getFirstTimeSlot(appointment);
        return WaitingRoom.builder()
                .client(client)
                .appointment(appointment)
                .arrivalDate(LocalDateTime.now())
                .appointmentTime(firstTS != null ? firstTS.getBegin() : null)
                .build();
    }

    private TimeSlot getFirstTimeSlot(Appointment appointment) {
        if (appointment != null) {
            List<TimeSlot> timeSlots = timeSlotRepository.findByAppointmentOrderByBeginAsc(appointment);
            return timeSlots.get(0);
        } else
            return null;
    }

    // Exceptions.

    private static class PlaceClientInWaitingRoomException extends RuntimeException {
        public PlaceClientInWaitingRoomException(String message) {
            super(message);
        }
    }

}
