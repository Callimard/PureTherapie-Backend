package puretherapie.crm.api.v1.waitingroom.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.data.agenda.TimeSlot;
import puretherapie.crm.data.agenda.repository.TimeSlotRepository;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.waitingroom.WaitingRoom;
import puretherapie.crm.data.waitingroom.repository.WaitingRoomRepository;
import puretherapie.crm.tool.ServiceTool;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static puretherapie.crm.tool.ServiceTool.generateErrors;

@Slf4j
@AllArgsConstructor
@Service
public class PlaceClientInWaitingRoomService {

    // Constants.

    public static final String CLIENT_PLACE_IN_WAITING_ROOM_SUCCESS = "client_place_in_waiting_room_success";
    public static final String CLIENT_PLACE_IN_WAITING_ROOM_FAIL = "client_place_in_waiting_room_fail";

    // ERRORS.

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found";
    public static final String NON_COHERENCE_BETWEEN_CLIENT_APPOINTMENT = "non_coherence_between_client_appointment";
    public static final String APPOINTMENT_CANCELED = "appointment_canceled";
    public static final String TIME_SLOT_INCOHERENCE = "time_slot_incoherence";
    public static final String APPOINTMENT_NOT_FOR_TODAY = "appointment_not_for_today";

    // Variables.

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WaitingRoomRepository waitingRoomRepository;

    // Methods.

    /**
     * @param idClient the sur booking client
     *
     * @return the result of the try to place client in waiting room
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> placeClientInWaitingRoom(int idClient) {
        return placeClientInWaitingRoom(idClient, -1);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> placeClientInWaitingRoom(int idClient, int idAppointment) {

        try {
            Client client = verifyClient(idClient);
            Appointment appointment = getAppointment(idAppointment);
            verifyClientAndAppointmentCoherence(client, appointment);
            verifyAppointmentIsForToday(appointment);
            createWaitingRoom(client, appointment);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to place client in waiting room", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private Client verifyClient(int idClient) {
        Client c = clientRepository.findByIdPerson(idClient);
        if (c == null)
            throw new PlaceClientInWaitingRoomException("Client not found", generateErrors(CLIENT_NOT_FOUND_ERROR,
                                                                                           "Client with id %s not found".formatted(idClient)));
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
            throw new PlaceClientInWaitingRoomException(
                    "Client with id %s is not link to the appointment (appointment.idClient = %s)".formatted(appointment.getClient().getIdPerson(),
                                                                                                             client.getIdPerson()),
                    generateErrors(NON_COHERENCE_BETWEEN_CLIENT_APPOINTMENT, "client not link to the appointment"));

        if (appointment.isCanceled())
            throw new PlaceClientInWaitingRoomException("Appointment canceled", generateErrors(APPOINTMENT_CANCELED, "Client appointment was " +
                    "canceled"));
    }

    private void verifyAppointmentIsForToday(Appointment appointment) {
        if (appointment != null) {
            List<TimeSlot> timeSlots = timeSlotRepository.findByAppointmentOrderByBeginAsc(appointment);
            verifyTimeSlot(timeSlots);
            TimeSlot first = timeSlots.get(0);
            if (!first.getDay().equals(today()))
                throw new PlaceClientInWaitingRoomException("Appointment is not for today %s but for the day %s".formatted(first.getDay(), today()),
                                                            generateErrors(APPOINTMENT_NOT_FOR_TODAY, "Appointment not for today"));
        }
    }

    private void verifyTimeSlot(List<TimeSlot> timeSlots) {
        for (TimeSlot timeSlot : timeSlots) {
            if (timeSlot == null || timeSlot.isFree())
                throw new PlaceClientInWaitingRoomException("Time slot null or free", generateErrors(TIME_SLOT_INCOHERENCE, "Time slot null or " +
                        "free"));
        }
    }

    private void createWaitingRoom(Client client, Appointment appointment) {
        WaitingRoom waitingRoom = buildWaitingRoom(client, appointment);
        waitingRoom = waitingRoomRepository.save(waitingRoom);
        log.debug("Save waiting room {}", waitingRoom);
    }

    private WaitingRoom buildWaitingRoom(Client client, Appointment appointment) {
        TimeSlot firstTS = getFirstTimeSlot(appointment);
        return WaitingRoom.builder()
                .client(client)
                .appointment(appointment)
                .arrivalDate(OffsetDateTime.now())
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

    private Map<String, Object> generateSuccessRes() {
        return Collections.singletonMap(CLIENT_PLACE_IN_WAITING_ROOM_SUCCESS, "client place in waiting room success");
    }

    private Map<String, Object> generateErrorRes(Exception e) {
        if (e instanceof PlaceClientInWaitingRoomException pcwrException)
            return Collections.singletonMap(CLIENT_PLACE_IN_WAITING_ROOM_FAIL, pcwrException.getErrors());
        else
            return Collections.singletonMap(CLIENT_PLACE_IN_WAITING_ROOM_FAIL, e.getMessage());
    }

    private LocalDate today() {
        return LocalDate.now();
    }

    // Exceptions.

    private static class PlaceClientInWaitingRoomException extends ServiceTool.ServiceException {
        public PlaceClientInWaitingRoomException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }

}
