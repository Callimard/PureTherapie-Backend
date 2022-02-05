package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.api.v1.waitingroom.service.PlaceInWaitingRoomService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.ClientArrival;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientArrivalRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static puretherapie.crm.api.v1.appointment.service.ClientDelayService.*;

@Slf4j
@AllArgsConstructor
@Service
public class ClientArrivalService {

    // Constants.

    public static final String CLIENT_ARRIVAL_SUCCESS = "client_arrival_success";
    public static final String CLIENT_ARRIVAL_FAIL = "client_arrival_fail";

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found_error";
    public static final String APPOINTMENT_NOT_FOR_TODAY_ERROR = "appointment_not_for_today_error";
    public static final String CLIENT_TOO_MUCH_LATE_ERROR = "client_too_much_late_error";
    public static final String WAITING_ROOM_ERROR = "waiting_room_error";

    // Variables.

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientArrivalRepository clientArrivalRepository;
    private final ClientDelayService clientDelayService;
    private final PlaceInWaitingRoomService placeInWaitingRoomService;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SimpleResponseDTO clientArrive(int idClient, int idAppointment) {
        try {
            Client client = verifyClient(idClient);
            ClientArrival clientArrival = buildClientArrival(client);
            clientArrival = saveClientArrival(clientArrival);
            Appointment appointment = getAppointment(idAppointment);
            if (appointment != null) {
                verifyAppointmentIsForToday(appointment);
                verifyClientDelay(appointment);
                appointment = linkAppointmentAndClientArrival(appointment, clientArrival);
            }
            placeClientInWaitingRoom(client, appointment);
            return SimpleResponseDTO.generateSuccess("Success client arrive");
        } catch (Exception e) {
            log.error("Exception", e);
            log.debug("Fail client arrival, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new ClientArrivalException(CLIENT_NOT_FOUND_ERROR);

        return client;
    }

    private Appointment getAppointment(int idAppointment) {
        return appointmentRepository.findByIdAppointment(idAppointment);
    }

    private ClientArrival buildClientArrival(Client client) {
        return ClientArrival.builder()
                .client(client)
                .arrivalDate(LocalDateTime.now())
                .build();
    }

    private ClientArrival saveClientArrival(ClientArrival clientArrival) {
        ClientArrival ca = clientArrivalRepository.save(clientArrival);
        log.debug("Save client arrival {}", ca);
        return ca;
    }

    private void verifyAppointmentIsForToday(Appointment appointment) {
        if (!appointment.getDay().equals(LocalDate.now()))
            throw new ClientArrivalException(APPOINTMENT_NOT_FOR_TODAY_ERROR);
    }

    private void verifyClientDelay(Appointment appointment) {
        if (isLateFromNow(appointment.getTime())) {
            if (isTooMuchLateFromNow(appointment.getTime())) {
                log.debug("Too much client delay ({} minutes)", delayFromNow(appointment.getTime()));
                throw new ClientArrivalException(CLIENT_TOO_MUCH_LATE_ERROR);
            } else {
                log.debug("Save client delay ({} minutes)", delayFromNow(appointment.getTime()));
                SimpleResponseDTO res = clientDelayService.createClientDelay(appointment.getClient(), appointment,
                                                                             (int) delayFromNow(appointment.getTime()));
                if (!res.success())
                    log.debug("Fail to create client delay");
            }
        }
    }

    private Appointment linkAppointmentAndClientArrival(Appointment appointment, ClientArrival clientArrival) {
        appointment.setClientArrival(clientArrival);
        appointment = appointmentRepository.save(appointment);
        log.debug("Link appointment and client arrival -> Appointment: {}", appointment);
        return appointment;
    }

    private void placeClientInWaitingRoom(Client client, Appointment appointment) {
        SimpleResponseDTO res = placeInWaitingRoomService.placeClient(client, appointment);
        if (!res.success()) {
            log.debug("Fail to place client in waiting room");
            throw new ClientArrivalException(WAITING_ROOM_ERROR);
        }
    }

    // Exception.

    private static class ClientArrivalException extends RuntimeException {

        public ClientArrivalException(String message) {
            super(message);
        }
    }

}
