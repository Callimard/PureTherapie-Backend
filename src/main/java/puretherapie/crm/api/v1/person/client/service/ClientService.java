package puretherapie.crm.api.v1.person.client.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ClientService {

    // Constants.

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found_error";

    // Variables.

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;

    // Methods.

    public boolean isNew(int idClient) {
        Client client = verifyClient(idClient);
        List<Appointment> clientAppointments = appointmentRepository.findByClient(client);
        return clientAppointments.isEmpty() || (clientAppointments.size() == 1 && !clientAppointments.get(0).isFinalized());
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new ClientServiceException(CLIENT_NOT_FOUND_ERROR);

        return client;
    }

    // Exceptions.

    public static class ClientServiceException extends RuntimeException {
        public ClientServiceException(String message) {
            super(message);
        }
    }

}
