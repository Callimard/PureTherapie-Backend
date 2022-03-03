package puretherapie.crm.api.v1.person.client.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import puretherapie.crm.api.v1.person.client.controller.dto.*;
import puretherapie.crm.api.v1.person.client.service.ClientRegistrationService;
import puretherapie.crm.api.v1.person.client.service.ClientService;
import puretherapie.crm.api.v1.person.client.service.ClientUpdateService;
import puretherapie.crm.api.v1.product.aesthetic.care.service.AestheticCareStockService;
import puretherapie.crm.api.v1.product.bill.service.PaymentService;
import puretherapie.crm.api.v1.user.controller.dto.PersonOriginDTO;
import puretherapie.crm.api.v1.util.StorageService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.appointment.repository.ClientAbsenceRepository;
import puretherapie.crm.data.appointment.repository.ClientDelayRepository;
import puretherapie.crm.data.person.PersonOrigin;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.repository.PersonOriginRepository;
import puretherapie.crm.tool.PhoneTool;
import puretherapie.crm.tool.StringTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.person.client.controller.ClientController.CLIENTS_URL;
import static puretherapie.crm.tool.PhoneTool.formatPhone;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(CLIENTS_URL)
public class ClientController {

    // Constants.

    private static final String UNKNOWN_CLIENT = "Unknown id Client";

    public static final String CLIENTS_URL = API_V1_URL + "/clients";

    private static final int SEARCH_CLIENT_FILTER_SPLIT_ARRAY_SIZE = 4;


    public static final String CLIENT_SEARCH_WITH_EMAIL = "/searchWithEmail";
    public static final String CLIENT_SEARCH_WITH_EMAIL_URL = CLIENTS_URL + CLIENT_SEARCH_WITH_EMAIL;

    public static final String CLIENT_SEARCH_WITH_PHONE = "/searchWithPhone";
    public static final String CLIENT_SEARCH_WITH_PHONE_URL = CLIENTS_URL + CLIENT_SEARCH_WITH_PHONE;

    public static final String PERSON_ORIGINS = "/person_origins";
    public static final String PERSON_ORIGINS_URL = CLIENTS_URL + PERSON_ORIGINS;

    public static final String PARAM_DOUBLOON_VERIFICATION = "doubloonVerification";

    public static final String CLIENT_IS_NEW = "/isNew";
    public static final String CLIENT_IS_NEW_URL = CLIENTS_URL + CLIENT_IS_NEW;

    public static final String CLIENT_CARDS = "/{idClient}/cards";
    public static final String CLIENT_CARDS_URL = CLIENTS_URL + CLIENT_CARDS;

    public static final String CLIENT_ABSENCES_DELAYS = "/absencesDelays";

    public static final String CLIENT_BASIC_APPOINTMENTS = "/basicAppointments";

    public static final String CLIENT_REMAINING_STOCKS_PAY = "/remainingStocksPay";

    // Variables.

    private final ClientService clientService;
    private final ClientRegistrationService clientRegistrationService;
    private final ClientUpdateService clientUpdateService;
    private final ClientRepository clientRepository;
    private final PersonOriginRepository personOriginRepository;
    private final ClientAbsenceRepository clientAbsenceRepository;
    private final ClientDelayRepository clientDelayRepository;
    private final AppointmentRepository appointmentRepository;
    private final AestheticCareStockService aestheticCareStockService;
    private final PaymentService paymentService;
    private final StorageService storageService;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping(CLIENT_CARDS)
    public void uploadClientCard(@PathVariable(name = "idClient") int idClient, @RequestParam("client_card") MultipartFile file) {
        storageService.storeClientCard(idClient, file);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping("/{idClient}" + CLIENT_REMAINING_STOCKS_PAY)
    public ClientRemainingStockPayDTO getClientRemainingStocksPay(@PathVariable(name = "idClient") int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client != null) {
            int totalStock = aestheticCareStockService.getTotalStock(idClient);
            double remainingToPay = paymentService.totalLeftToPay(client);
            return new ClientRemainingStockPayDTO(totalStock, remainingToPay);
        } else
            throw new IllegalArgumentException(UNKNOWN_CLIENT);
    }


    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping("/{idClient}" + CLIENT_BASIC_APPOINTMENTS)
    public ClientBasicAppointmentDTO getClientBasicAppointments(@PathVariable(name = "idClient") int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client != null) {
            List<Appointment> appointments = appointmentRepository.findByClientOrderByDayAsc(client);
            if (!appointments.isEmpty()) {
                return new ClientBasicAppointmentDTO(appointments.get(0).transform(), appointments.get(appointments.size() - 1).transform());
            } else {
                return null;
            }
        } else
            throw new IllegalArgumentException(UNKNOWN_CLIENT);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping("/{idClient}" + CLIENT_ABSENCES_DELAYS)
    public ClientAbsenceDelayDTO getClientAbsencesDelays(@PathVariable(name = "idClient") int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client != null) {
            int nbAbsence = clientAbsenceRepository.findByClient(client).size();
            int nbDelay = clientDelayRepository.findByClient(client).size();
            return new ClientAbsenceDelayDTO(nbAbsence, nbDelay);
        } else
            throw new IllegalArgumentException(UNKNOWN_CLIENT);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(CLIENT_IS_NEW)
    public Boolean clientIsNew(@RequestParam(name = "idClient") int idClient) {
        return clientService.isNew(idClient);
    }

    @PostMapping
    public ResponseEntity<ClientRegistrationResponseDTO> clientRegistration(@RequestParam(value = PARAM_DOUBLOON_VERIFICATION, required = false,
            defaultValue = "true") boolean doubloonVerification, @RequestBody ClientDTO clientDTO,
                                                                            Authentication authentication) {

        doubloonVerification = verifyPermissionForDoubloonVerification(doubloonVerification, authentication);

        ClientRegistrationResponseDTO responseDTO = clientRegistrationService.clientRegistration(clientDTO, doubloonVerification);
        if (responseDTO.isFailedResponse())
            return ResponseEntity.badRequest().body(responseDTO);
        else
            return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping("/{clientId}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable(name = "clientId") int clientId, @RequestBody ClientDTO clientDTO) {
        if (clientDTO.getIdPerson() != clientId) {
            log.error("Update a client with id {} but client data has different id {}", clientId, clientDTO.getIdPerson());
            return ResponseEntity.badRequest().body(null);
        }

        ClientDTO client = this.clientUpdateService.updateClient(clientDTO);

        if (client == null) {
            log.error("Fail to update client with the id {}", clientId);
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(client);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping
    public ResponseEntity<List<ClientDTO>> getClientWithFilter(@RequestParam("filter") String filter,
                                                               @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                               @RequestParam(value = "pageSize", required = false, defaultValue = "40") int pageSize) {
        try {
            Pageable pageable = PageRequest.of(page, pageSize);
            List<Client> clients = new ClientSearchFilter(filter).search(clientRepository, pageable);
            return ResponseEntity.ok(clients.stream().map(Client::transform).toList());
        } catch (IllegalArgumentException e) {
            log.debug("Wrong filter format => {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    private boolean verifyPermissionForDoubloonVerification(boolean doubloonVerification, Authentication authentication) {
        if ((authentication == null || !authentication.isAuthenticated()) && !doubloonVerification) {
            log.info("Cannot avoid doubloon verification if no authenticated");
            doubloonVerification = true;
        }
        return doubloonVerification;
    }

    @GetMapping(CLIENT_SEARCH_WITH_EMAIL)
    public SimpleClientInfoDTO getClientWithEmail(@RequestParam(value = "email") String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail);
        if (client != null)
            return client.transformSimple();
        else
            return null;
    }

    @GetMapping(CLIENT_SEARCH_WITH_PHONE)
    public ResponseEntity<SimpleClientInfoDTO> getClientWithPhone(@RequestParam(value = "phone") String clientPhone) {
        try {
            Client client = clientRepository.findByPhone(formatPhone(clientPhone));
            if (client != null)
                return ResponseEntity.ok(client.transformSimple());
            else
                return ResponseEntity.ok(null);
        } catch (Exception e) {
            log.error("Fail to get client with phone, Err msg = {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping(PERSON_ORIGINS)
    public ResponseEntity<List<PersonOriginDTO>> getAllPersonOrigin() {
        List<PersonOrigin> personOrigins = personOriginRepository.findAll();
        List<PersonOriginDTO> allPersonOrigins = new ArrayList<>();

        for (PersonOrigin personOrigin : personOrigins) {
            allPersonOrigins.add(personOrigin.buildDTO());
        }

        return ResponseEntity.ok(allPersonOrigins);
    }

    @Builder
    @Getter
    @ToString
    @AllArgsConstructor
    private static class ClientSearchFilter {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;

        public ClientSearchFilter(String filter) {
            this.extractAndSet(filter);
        }

        private void extractAndSet(String filter) {
            String[] data = filter.split(" ");
            verifyFilterFormat(filter, data);
            extractFilterValue(data);
        }

        private void verifyFilterFormat(String filter, String[] splitFilter) {
            if (splitFilter.length != SEARCH_CLIENT_FILTER_SPLIT_ARRAY_SIZE)
                throw new IllegalArgumentException("Search client filter not correctly formatted, expected must be filter = firstName=value " +
                                                           "lastName=value email=value phone=value, current = " + filter);
        }

        private void extractFilterValue(String[] data) {
            for (String d : data) {
                String[] dataSplit = d.split("=");
                String key = dataSplit[0];
                if (dataSplit.length == 2)
                    setValue(key, dataSplit[1]);
            }
        }

        private void setValue(String key, String value) {
            switch (key) {
                case "firstName" -> this.setFirstName(value);
                case "lastName" -> this.setLastName(value);
                case "email" -> this.setEmail(value);
                case "phone" -> this.setPhone(value);
                default -> throw new IllegalArgumentException("Search client filter argument unknown, filter key = " + key);
            }
        }

        public void setFirstName(String firstName) {
            if (correctValue(firstName))
                this.firstName = firstName.toLowerCase();
        }

        public void setLastName(String lastName) {
            if (correctValue(lastName))
                this.lastName = lastName.toLowerCase();
        }

        public void setEmail(String email) {
            if (correctValue(email))
                this.email = email.toLowerCase();
        }

        public void setPhone(String phone) {
            if (correctValue(phone)) {
                try {
                    this.phone = StringTool.removeRemainingSpaces(PhoneTool.permissiveFormatPhone(phone));
                } catch (PhoneTool.UnSupportedPhoneNumberException | PhoneTool.NotPhoneNumberException | PhoneTool.FailToFormatPhoneNumber e) {
                    this.phone = null;
                }
            }
        }

        private boolean correctValue(String value) {
            return value != null && !value.isBlank();
        }

        private List<Client> search(ClientRepository clientRepository, Pageable pageable) {
            String firstNameFilter = firstName != null && !firstName.isBlank() ? firstName + "%" : "%";
            String lastNameFilter = lastName != null && !lastName.isBlank() ? lastName + "%" : "%";
            String emailFilter = email != null && !email.isBlank() ? email + "%" : "%";
            String phoneFilter = phone != null && !phone.isBlank() ? phone + "%" : "%";

            return clientRepository.findByFirstNameLikeAndLastNameLikeAndEmailLikeAndPhoneLike(firstNameFilter, lastNameFilter, emailFilter,
                                                                                               phoneFilter, pageable);
        }
    }
}
