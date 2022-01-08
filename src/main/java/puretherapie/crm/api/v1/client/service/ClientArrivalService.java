package puretherapie.crm.api.v1.client.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.appointment.service.ClientDelayService;
import puretherapie.crm.api.v1.waitingroom.service.PlaceClientInWaitingRoomService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.ClientArrival;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientArrivalRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.tool.ServiceTool;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

import static puretherapie.crm.api.v1.appointment.service.ClientDelayService.*;
import static puretherapie.crm.api.v1.waitingroom.service.PlaceClientInWaitingRoomService.hasSuccess;
import static puretherapie.crm.tool.ServiceTool.generateError;
import static puretherapie.crm.tool.TimeTool.today;

@Slf4j
@AllArgsConstructor
@Service
public class ClientArrivalService {

    // Constants.

    public static final String CLIENT_ARRIVAL_SUCCESS = "client_arrival_success";
    public static final String CLIENT_ARRIVAL_FAIL = "client_arrival_fail";
    public static final String UNKNOWN_ERROR = "unknown_error";
    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found_error";
    public static final String CLIENT_TOO_MUCH_LATE_ERROR = "client_too_much_late_error";
    public static final String WAITING_ROOM_ERROR = "waiting_room_error";

    // Variables.

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientArrivalRepository clientArrivalRepository;
    private final PlaceClientInWaitingRoomService placeClientInWaitingRoomService;
    private final ClientDelayService clientDelayService;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> clientArrive(int idClient) {
        try {
            Client client = verifyClient(idClient);
            Appointment appointment = getAppointment(client, today());
            ClientArrival clientArrival = buildClientArrival(client);
            clientArrival = saveClientArrival(clientArrival);
            if (appointment != null) {
                verifyClientDelay(appointment);
                appointment = linkAppointmentAndClientArrival(appointment, clientArrival);
            }
            placeClientInWaitingRoom(client, appointment);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail client arrival, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new ClientArrivalException("Client not found", generateError(CLIENT_NOT_FOUND_ERROR, "Client not found"));

        return client;
    }

    private Appointment getAppointment(Client client, LocalDate today) {
        return appointmentRepository.findByClientAndDay(client, today);
    }

    private ClientArrival buildClientArrival(Client client) {
        return ClientArrival.builder()
                .client(client)
                .arrivalDate(OffsetDateTime.now())
                .build();
    }

    private ClientArrival saveClientArrival(ClientArrival clientArrival) {
        ClientArrival ca = clientArrivalRepository.save(clientArrival);
        log.debug("Save client arrival {}", ca);
        return ca;
    }

    private void verifyClientDelay(Appointment appointment) {
        if (isLateFromNow(appointment.getTime())) {
            if (isTooMuchLateFromNow(appointment.getTime())) {
                log.debug("Too much client delay ({} minutes)", delayFromNow(appointment.getTime()));
                throw new ClientArrivalException("Client too much late", generateError(CLIENT_TOO_MUCH_LATE_ERROR,
                                                                                       "Client to much late (%s minutes)".formatted(
                                                                                               delayFromNow(appointment.getTime()))));
            } else {
                log.debug("Save client delay ({} minutes)", delayFromNow(appointment.getTime()));
                clientDelayService.createClientDelay(appointment.getClient(), appointment, (int) delayFromNow(appointment.getTime()));
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
        Map<String, Object> res = placeClientInWaitingRoomService.placeClientInWaitingRoom(client, appointment);
        if (!hasSuccess(res)) {
            log.debug("Fail to place client in waiting room");
            throw new ClientArrivalException("Fail to place the client in waiting room", generateError(WAITING_ROOM_ERROR, "Fail to place client in" +
                    " waiting room"));
        }
    }

    private Map<String, Object> generateSuccessRes() {
        return Collections.singletonMap(CLIENT_ARRIVAL_SUCCESS, "Client arrival success");
    }

    private Map<String, Object> generateErrorRes(Exception e) {
        if (e instanceof ClientArrivalException ace) {
            return Collections.singletonMap(CLIENT_ARRIVAL_FAIL, ace.getErrors());
        } else {
            return Collections.singletonMap(UNKNOWN_ERROR, e.getMessage());
        }
    }

    // Exception.

    private static class ClientArrivalException extends ServiceTool.ServiceException {

        public ClientArrivalException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }

}
