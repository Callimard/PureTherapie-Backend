package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.historical.service.HistoricalCreationService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
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

import static puretherapie.crm.api.v1.SimpleService.DATA_DIRECTORY_PATH;
import static puretherapie.crm.api.v1.SimpleService.createDataDirectory;
import static puretherapie.crm.data.historical.HistoricalLevel.BOSS_LEVEL;
import static puretherapie.crm.tool.TimeTool.minuteBetween;

@Slf4j
@AllArgsConstructor
@Service
public class ClientDelayService {

    // Constants.

    public static final String CLIENT_DELAY_TITLE = "Retard client";
    public static final String CLIENT_DELAY_TEXT = "Le client %s est arrivé en retard pour son RDV de %s ";

    public static final String MAXIMUM_CLIENT_DELAY_FILE = DATA_DIRECTORY_PATH + "/max_client_delay.data";

    public static final int DEFAULT_MAXIMUM_CLIENT_DELAY = 15;
    private static int maximumClientDelay = chargeMaximumClientDelay();

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found";
    public static final String APPOINTMENT_NOT_FOUND_ERROR = "appointment_not_found";
    public static final String CLIENT_NOT_ASSOCIATE_TO_APPOINTMENT_ERROR = "client_not_associate_to_appointment";

    // Variables.

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientDelayRepository clientDelayRepository;
    private final HistoricalCreationService historicalCreationService;

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
     * @return true if the appointment time is before the now time ({@link LocalTime#now()}), else false.
     */
    public static boolean isLateFromNow(LocalTime appointmentTime) {
        return appointmentTime.isBefore(LocalTime.now());
    }

    public static boolean isTooMuchLateFromNow(LocalTime appointmentTime) {
        if (isLateFromNow(appointmentTime)) {
            long diff = minuteBetween(appointmentTime, LocalTime.now());
            return diff >= getMaximumClientDelay();
        } else
            return false;
    }

    public static long delayFromNow(LocalTime appointmentTime) {
        if (isLateFromNow(appointmentTime))
            return minuteBetween(appointmentTime, LocalTime.now());
        else
            return 0;
    }

    // Service methods.

    public SimpleResponseDTO createClientDelay(Client client, Appointment appointment, int delay) {
        return createClientDelay(client.getIdPerson(), appointment.getIdAppointment(), delay);
    }

    public SimpleResponseDTO createClientDelay(int idClient, int idAppointment, int delay) {
        try {
            Client client = verifyClient(idClient);
            Appointment appointment = verifyAppointment(idAppointment);
            verifyClientAssociateToAppointment(client, appointment);
            delay = verifyDelay(delay);
            saveClientDelay(client, appointment, delay);
            notifyClientDelay(client, appointment.getTime());
            return SimpleResponseDTO.generateSuccess("Success to create client delay");
        } catch (Exception e) {
            log.debug("Fail to create client delay: {}", e.getMessage());
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new ClientDelayCreationException(CLIENT_NOT_FOUND_ERROR);
        return client;
    }

    private Appointment verifyAppointment(int idAppointment) {
        Appointment appointment = appointmentRepository.findByIdAppointment(idAppointment);
        if (appointment == null)
            throw new ClientDelayCreationException(APPOINTMENT_NOT_FOUND_ERROR);
        return appointment;
    }

    private void verifyClientAssociateToAppointment(Client client, Appointment appointment) {
        if (!client.isAssociateTo(appointment))
            throw new ClientDelayCreationException(CLIENT_NOT_ASSOCIATE_TO_APPOINTMENT_ERROR);

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

    private void notifyClientDelay(Client client, LocalTime time) {
        boolean success = historicalCreationService.createHistorical(CLIENT_DELAY_TITLE,
                                                                     CLIENT_DELAY_TEXT.formatted(client.simplyIdentifier(), time),
                                                                     BOSS_LEVEL, false);
        if (!success)
            log.error("Fail to create client delay notification");
    }

    // Exceptions.

    private static class ClientDelayCreationException extends RuntimeException {
        public ClientDelayCreationException(String message) {
            super(message);
        }
    }
}
