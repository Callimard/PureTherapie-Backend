package puretherapie.crm.api.v1.appointment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.product.bill.service.PaymentService;
import puretherapie.crm.data.appointment.Surbooking;
import puretherapie.crm.data.appointment.repository.SurbookingRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class SurbookingService {

    // Variables.

    private final SurbookingRepository surbookingRepository;
    private final ClientRepository clientRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final PaymentService paymentService;
    private final ClientArrivalService clientArrivalService;

    // Methods.

    public List<Surbooking> getAllSurbooking(LocalDate day, boolean canceled) {
        return surbookingRepository.findByDayAndCanceled(day, canceled);
    }

    public void createSurbooking(int idClient, int idAC, LocalDate day, LocalTime time) {
        Client client = clientRepository.findByIdPerson(idClient);
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAC);
        saveSurbooking(day, time, client, ac);
    }

    private void saveSurbooking(LocalDate day, LocalTime time, Client client, AestheticCare ac) {
        Surbooking surbooking = surbookingRepository.save(buildSurbooking(day, time, client, ac));
        log.info("Save surbooking => {}", surbooking);
    }

    private Surbooking buildSurbooking(LocalDate day, LocalTime time, Client client, AestheticCare ac) {
        return Surbooking.builder()
                .client(client)
                .aestheticCare(ac)
                .day(day)
                .time(time)
                .build();
    }

    public void cancelSurbooking(int idSurbooking) {
        Surbooking surbooking = surbookingRepository.findByIdSurbooking(idSurbooking);
        if (!surbooking.isCanceled() && !surbooking.isFinalized()) {
            cancelSurbooking(surbooking);
        } else
            log.info("Surbooking already canceled or finalized");
    }

    private void cancelSurbooking(Surbooking surbooking) {
        surbooking.setCanceled(true);
        surbooking = surbookingRepository.save(surbooking);
        log.info("Cancel surbooking => {}", surbooking);
    }

    public void clientArrive(int idSurbooking) {
        Surbooking surbooking = surbookingRepository.findByIdSurbooking(idSurbooking);
        clientArrivalService.clientArriveForSurbooking(surbooking.getClient().getIdPerson(), surbooking.getIdSurbooking());
    }

    public void finalizedSurbooking(int idSurbooking) {
        Surbooking surbooking = surbookingRepository.findByIdSurbooking(idSurbooking);
        verifyNotFinalized(surbooking);
        verifyNotCanceled(surbooking);
        verifyClientArrived(surbooking);
        verifyClientHasDoneAtLeastOnePaymentToday(surbooking.getClient());
        finalizedSurbooking(surbooking);
    }

    private void verifyNotFinalized(Surbooking surbooking) {
        if (surbooking.isFinalized())
            throw new SurbookingException("Surbooking already finalized");
    }

    private void verifyNotCanceled(Surbooking surbooking) {
        if (surbooking.isCanceled())
            throw new SurbookingException("Surbooking canceled, cannot finalized the surbooking");
    }

    private void verifyClientArrived(Surbooking surbooking) {
        if (surbooking.getClientArrival() == null)
            throw new SurbookingException("Client is not arrived, cannot finalized the surbooking");
    }

    private void verifyClientHasDoneAtLeastOnePaymentToday(Client client) {
        if (paymentService.hasRemainingPayment(client) && !paymentService.hasDonePaymentToday(client)) {
            throw new SurbookingException("Client has not paid today");
        }
    }

    private void finalizedSurbooking(Surbooking surbooking) {
        surbooking.setFinalized(true);
        surbooking = surbookingRepository.save(surbooking);
        log.info("Finalized surbooking => {}", surbooking);
    }

    // Exception

    public static class SurbookingException extends RuntimeException {
        public SurbookingException(String message) {
            super(message);
        }
    }
}
