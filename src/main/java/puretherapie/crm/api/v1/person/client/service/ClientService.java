package puretherapie.crm.api.v1.person.client.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.appointment.repository.AppointmentRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.tool.PhoneTool;
import puretherapie.crm.tool.StringTool;

import java.time.LocalDate;
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

    /**
     * Verify if the client at the moment of the call of the methods.
     *
     * @param idClient the client id
     *
     * @return true if the client is new when we call the method.
     */
    public boolean isNew(int idClient) {
        Client client = verifyClient(idClient);
        Appointment appointment = getClientFirstAppointment(client);
        if (appointment != null) {
            LocalDate today = LocalDate.now();
            return appointment.getDay().isAfter(today) || appointment.getDay().equals(today);
        } else
            return true;
    }

    public Appointment getClientFirstAppointment(int idClient) {
        Client client = verifyClient(idClient);
        return getClientFirstAppointment(client);
    }

    public Appointment getClientFirstAppointment(Client client) {
        List<Appointment> clientAppointments = appointmentRepository.findByClient(client);
        for (Appointment appointment : clientAppointments) {
            if (!appointment.isCanceled())
                return appointment;
        }

        // Has no appointment so new client.
        return null;
    }


    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new ClientServiceException(CLIENT_NOT_FOUND_ERROR);

        return client;
    }

    public List<Client> searchClientWithFilter(String filter, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return new ClientService.ClientSearchFilter(filter).search(clientRepository, pageable);
    }

    // Exceptions.

    public static class ClientServiceException extends RuntimeException {
        public ClientServiceException(String message) {
            super(message);
        }
    }

    // Inner classes.

    @Builder
    @Getter
    @ToString
    @AllArgsConstructor
    private static class ClientSearchFilter {

        /**
         * First name or Last name.
         */
        private String name;
        private String email;
        private String phone;

        public ClientSearchFilter(String filter) {
            this.extractAndSet(filter);
        }

        private void extractAndSet(String filter) {
            String[] data = filter.split(" ");
            extractFilterValue(data);
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
                case "name" -> this.setName(value);
                case "email" -> this.setEmail(value);
                case "phone" -> this.setPhone(value);
                default -> throw new IllegalArgumentException("Search client filter argument unknown, filter key = " + key);
            }
        }

        public void setName(String name) {
            if (correctValue(name))
                this.name = name.toLowerCase();
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
            if (email != null && !email.isBlank()) {
                String emailFilter = email + "%";
                return clientRepository.findByEmailLike(emailFilter, pageable);
            } else if (phone != null && !phone.isBlank()) {
                String phoneFilter = phone + "%";
                return clientRepository.findByPhoneLike(phoneFilter, pageable);
            } else {
                String nameFilter = name != null && !name.isBlank() ? name + "%" : "%";
                return clientRepository.findByFirstNameLikeOrLastNameLike(nameFilter, nameFilter, pageable);
            }
        }
    }

}
