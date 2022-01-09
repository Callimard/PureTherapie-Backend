package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.ClientDelay;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientDelayRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Map;

import static puretherapie.crm.tool.TimeTool.minuteBetween;

@Slf4j
@AllArgsConstructor
@Service
public class ClientDelayService extends SimpleService {

    // Constants.

    public static final String MAXIMUM_CLIENT_DELAY_FILE = DATA_DIRECTORY_PATH + "/max_client_delay.data";

    public static final int DEFAULT_MAXIMUM_CLIENT_DELAY = 15;
    private static int maximumClientDelay = chargeMaximumClientDelay();

    public static final String CLIENT_DELAY_CREATION_SUCCESS = "client_delay_creation_success";
    public static final String CLIENT_DELAY_CREATION_FAIL = "client_delay_creation_fail";

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found";
    public static final String APPOINTMENT_NOT_FOUND_ERROR = "appointment_not_found";
    public static final String CLIENT_NOT_ASSOCIATE_TO_APPOINTMENT_ERROR = "client_not_associate_to_appointment";

    // Variables.

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientDelayRepository clientDelayRepository;

    // Methods.

    public static synchronized int getMaximumClientDelay() {
        return maximumClientDelay;
    }

    public static synchronized void setMaximumClientDelay(int maximumClientDelay) {
        if (maximumClientDelay < 0) {
            log.debug("Maximum delay set is negative (delay = {}) set it to 0", maximumClientDelay);
            maximumClientDelay = 0;
        }

        ClientDelayService.maximumClientDelay = maximumClientDelay;
        saveMaximumClientDelay();
    }

    public static void rechargeMaximumClientDelay() {
        setMaximumClientDelay(chargeMaximumClientDelay());
    }

    private static int chargeMaximumClientDelay() {
        if (Files.exists(Path.of(MAXIMUM_CLIENT_DELAY_FILE))) {
            try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(MAXIMUM_CLIENT_DELAY_FILE))) {
                return reader.readInt();
            } catch (IOException e) {
                log.debug("Fail to charge maximumClientDelay", e);
                return DEFAULT_MAXIMUM_CLIENT_DELAY;
            }
        } else
            return DEFAULT_MAXIMUM_CLIENT_DELAY;
    }

    private static void saveMaximumClientDelay() {
        try {
            createDataDirectory();
        } catch (IOException e) {
            log.debug("Fail to create data directory", e);
        }

        try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(MAXIMUM_CLIENT_DELAY_FILE))) {
            if (!Files.exists(Path.of(MAXIMUM_CLIENT_DELAY_FILE)))
                Files.createFile(Path.of(MAXIMUM_CLIENT_DELAY_FILE));

            writer.writeInt(maximumClientDelay);
            writer.flush();
        } catch (IOException e) {
            log.debug("Fail to save maximumClientDelay", e);
        }
    }

    /**
     * @param appointmentTime the appointment time
     *
     * @return true if the appointment time is after the now time ({@link LocalTime#now()}), else false.
     */
    public static boolean isLateFromNow(LocalTime appointmentTime) {
        return appointmentTime.isAfter(LocalTime.now());
    }

    public static boolean isTooMuchLateFromNow(LocalTime appointmentTime) {
        if (isLateFromNow(appointmentTime)) {
            long diff = minuteBetween(LocalTime.now(), appointmentTime);
            return diff >= getMaximumClientDelay();
        } else
            return false;
    }

    public static long delayFromNow(LocalTime appointmentTime) {
        if (isLateFromNow(appointmentTime))
            return minuteBetween(LocalTime.now(), appointmentTime);
        else
            return 0;
    }

    // Service methods.

    public Map<String, Object> createClientDelay(Client client, Appointment appointment, int delay) {
        return createClientDelay(client.getIdPerson(), appointment.getIdAppointment(), delay);
    }

    public Map<String, Object> createClientDelay(int idClient, int idAppointment, int delay) {
        try {
            Client client = verifyClient(idClient);
            Appointment appointment = verifyAppointment(idAppointment);
            verifyClientAssociateToAppointment(client, appointment);
            delay = verifyDelay(delay);
            saveClientDelay(client, appointment, delay);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to create client delay: {}", e.getMessage());
            return generateErrorRes(e);
        }
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new ClientDelayCreationException("Client not found", generateError(CLIENT_NOT_FOUND_ERROR, "Client not found"));
        return client;
    }

    private Appointment verifyAppointment(int idAppointment) {
        Appointment appointment = appointmentRepository.findByIdAppointment(idAppointment);
        if (appointment == null)
            throw new ClientDelayCreationException("Appointment not found", generateError(APPOINTMENT_NOT_FOUND_ERROR, "Appointment not found"));
        return appointment;
    }

    private void verifyClientAssociateToAppointment(Client client, Appointment appointment) {
        if (!client.isAssociateTo(appointment))
            throw new ClientDelayCreationException("Client is not associate to the appointment",
                                                   generateError(CLIENT_NOT_ASSOCIATE_TO_APPOINTMENT_ERROR,
                                                                 "Client not associate to appointment"));

    }

    private int verifyDelay(int delay) {
        if (delay < 1) {
            log.debug("Delay is negative (delay = {}) set it to 1", delay);
            delay = 1;
        }

        return delay;
    }

    private void saveClientDelay(Client client, Appointment appointment, int delay) {
        ClientDelay clientDelay = buildClientDelay(client, appointment, delay);
        clientDelay = clientDelayRepository.save(clientDelay);
        log.debug("Save client delay {}", clientDelay);
    }

    private ClientDelay buildClientDelay(Client client, Appointment appointment, int delay) {
        return ClientDelay.builder()
                .client(client)
                .appointment(appointment)
                .delayTime(delay)
                .build();
    }

    // SimpleService methods.

    @Override
    public String getSuccessTag() {
        return CLIENT_DELAY_CREATION_SUCCESS;
    }

    @Override
    public String getFailTag() {
        return CLIENT_DELAY_CREATION_FAIL;
    }

    // Exceptions.

    private static class ClientDelayCreationException extends SimpleService.ServiceException {
        public ClientDelayCreationException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }
}
