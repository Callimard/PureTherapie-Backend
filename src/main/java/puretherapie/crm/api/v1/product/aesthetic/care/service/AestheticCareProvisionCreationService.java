package puretherapie.crm.api.v1.product.aesthetic.care.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.technician.Technician;
import puretherapie.crm.data.person.technician.repository.TechnicianRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.AestheticCareProvision;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareProvisionRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;
import puretherapie.crm.tool.ServiceTool;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

import static puretherapie.crm.tool.ServiceTool.generateError;

@Slf4j
@AllArgsConstructor
@Service
public class AestheticCareProvisionCreationService {

    // Constants.

    public static final String AC_PROVISION_CREATION_SUCCESS = "ac_provision_success";
    public static final String AC_PROVISION_CREATION_FAIL = "ac_provision_fail";

    public static final String NO_DATE_TIME_ERROR = "no_date_time";
    public static final String CLIENT_NOT_FOUND_ERROR = "client__not_found";
    public static final String TECHNICIAN_NOT_FOUND_ERROR = "technician_not_found";
    public static final String AESTHETIC_CARE_NOT_FOUND_ERROR = "ac_not_found";
    public static final String CLIENT_AND_APPOINTMENT_NOT_ASSOCIATED_ERROR = "client_and_appointment_not_associated";
    public static final String APPOINTMENT_CANCELED_ERROR = "appointment_canceled";

    // Variables.

    private final ClientRepository clientRepository;
    private final TechnicianRepository technicianRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final AppointmentRepository appointmentRepository;
    private final AestheticCareProvisionRepository aestheticCareProvisionRepository;

    // Methods.

    @Transactional(propagation = Propagation.SUPPORTS)
    public Map<String, Object> createAestheticCareProvision(int idClient, int idTechnician, int idAestheticCare,
                                                            int idAppointment, OffsetDateTime dateTime) {
        try {
            verifyDateTime(dateTime);
            Client client = verifyClient(idClient);
            Technician technician = verifyTechnician(idTechnician);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);
            Appointment appointment = getAppointment(idAppointment);
            verifyClientAndAppointmentCoherence(client, appointment);
            saveACProvision(client, technician, aestheticCare, appointment, dateTime);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to create aesthetic care provision", e);
            return generateErrorRes(e);
        }
    }

    private void verifyDateTime(OffsetDateTime dateTime) {
        if (dateTime == null)
            throw new AestheticCareProvisionCreationException("Null date time for aesthetic care provision", generateError(NO_DATE_TIME_ERROR, "No " +
                    "date time for aesthetic care provision"));
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new AestheticCareProvisionCreationException("Client not found", generateError(CLIENT_NOT_FOUND_ERROR, "Client not found"));

        return client;
    }

    private Technician verifyTechnician(int idTechnician) {
        Technician technician = technicianRepository.findByIdPerson(idTechnician);
        if (technician == null)
            throw new AestheticCareProvisionCreationException("Technician not found", generateError(TECHNICIAN_NOT_FOUND_ERROR, "Technician not " +
                    "found"));

        return technician;
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        if (ac == null)
            throw new AestheticCareProvisionCreationException("Aesthetic care not found", generateError(AESTHETIC_CARE_NOT_FOUND_ERROR, "Aesthetic " +
                    "care not found"));

        return ac;
    }

    private Appointment getAppointment(int idAppointment) {
        return appointmentRepository.findByIdAppointment(idAppointment);
    }

    private void verifyClientAndAppointmentCoherence(Client client, Appointment appointment) {
        if (appointment == null) {
            log.debug("Appointment null -> sur booking");
            return;
        }

        if (!appointment.getClient().getIdPerson().equals(client.getIdPerson()))
            throw new AestheticCareProvisionCreationException("Client and appointment not associated",
                                                              generateError(CLIENT_AND_APPOINTMENT_NOT_ASSOCIATED_ERROR, "client and appointment " +
                                                                      "are not associated"));

        if (appointment.isCanceled())
            throw new AestheticCareProvisionCreationException("Appointment is canceled", generateError(APPOINTMENT_CANCELED_ERROR, "Appointment is " +
                    "canceled"));
    }

    private void saveACProvision(Client client, Technician technician, AestheticCare aestheticCare, Appointment appointment,
                                 OffsetDateTime dateTime) {
        AestheticCareProvision aestheticCareProvision = buildAestheticCareProvision(client, technician, aestheticCare, appointment, dateTime);
        aestheticCareProvisionRepository.save(aestheticCareProvision);
    }

    private AestheticCareProvision buildAestheticCareProvision(Client client, Technician technician, AestheticCare aestheticCare,
                                                               Appointment appointment,
                                                               OffsetDateTime dateTime) {
        return AestheticCareProvision.builder()
                .date(dateTime)
                .client(client)
                .technician(technician)
                .aestheticCare(aestheticCare)
                .appointment(appointment)
                .build();
    }

    private Map<String, Object> generateSuccessRes() {
        return Collections.singletonMap(AC_PROVISION_CREATION_SUCCESS, "client place in waiting room success");
    }

    private Map<String, Object> generateErrorRes(Exception e) {
        if (e instanceof AestheticCareProvisionCreationService.AestheticCareProvisionCreationException accException)
            return Collections.singletonMap(AC_PROVISION_CREATION_FAIL, accException.getErrors());
        else
            return Collections.singletonMap(AC_PROVISION_CREATION_FAIL, e.getMessage());
    }

    // Exceptions.

    private static class AestheticCareProvisionCreationException extends ServiceTool.ServiceException {
        public AestheticCareProvisionCreationException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }

}
