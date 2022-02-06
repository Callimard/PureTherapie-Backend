package puretherapie.crm.api.v1.product.aesthetic.care.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundlePurchaseRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class AestheticCareStockService {

    // Constants.

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found_error";
    public static final String AESTHETIC_CARE_NOT_FOUND_ERROR = "aesthetic_care_not_found";

    // Variables.

    private final ClientRepository clientRepository;
    private final AestheticCareRepository aestheticCareRepository;
    private final SessionPurchaseRepository sessionPurchaseRepository;
    private final BundlePurchaseRepository bundlePurchaseRepository;

    // Methods.

    /**
     * Search the ac stock of the client of the specified ac.
     * <p>
     * Search in {@link puretherapie.crm.data.product.aesthetic.care.SessionPurchase} not used and {@link
     * puretherapie.crm.data.product.aesthetic.bundle.Stock} in {@link puretherapie.crm.data.product.aesthetic.bundle.Bundle} that the client has.
     *
     * @param idClient        the client id
     * @param idAestheticCare the ac id
     *
     * @return the ac stock of the client, -1 if the method fail.
     */
    public int getACStockOfClient(int idClient, int idAestheticCare) {
        try {
            Client client = verifyClient(idClient);
            AestheticCare aestheticCare = verifyAestheticCare(idAestheticCare);
            return sessionPurchaseStock(client, aestheticCare) + bundleStock(client, aestheticCare);
        } catch (Exception e) {
            log.error("Fail to get AC stock of the client. Err msg = {}", e.getMessage());
            return -1;
        }
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new AestheticCareStockException(CLIENT_NOT_FOUND_ERROR);
        return client;
    }

    private AestheticCare verifyAestheticCare(int idAestheticCare) {
        AestheticCare ac = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        if (ac == null)
            throw new AestheticCareStockException(AESTHETIC_CARE_NOT_FOUND_ERROR);
        return ac;
    }

    private int sessionPurchaseStock(Client client, AestheticCare aestheticCare) {
        List<SessionPurchase> clientSessionPurchases = sessionPurchaseRepository.findByClientAndAestheticCare(client, aestheticCare);
        if (clientSessionPurchases != null && !clientSessionPurchases.isEmpty()) {
            int count = 0;
            for (SessionPurchase sessionPurchase : clientSessionPurchases)
                if (!sessionPurchase.isUsed())
                    count++;
            return count;
        } else {
            return 0;
        }
    }

    private int bundleStock(Client client, AestheticCare aestheticCare) {
        List<BundlePurchase> clientBundlePurchases = bundlePurchaseRepository.findByClient(client);
        int count = 0;
        for (BundlePurchase bundlePurchase : clientBundlePurchases) {
            List<Stock> stocks = bundlePurchase.getStocks();
            for (Stock stock : stocks)
                if (stock.isAssociatedTo(aestheticCare))
                    count += stock.getRemainingQuantity();
        }
        return count;
    }

    // Exceptions.

    private static class AestheticCareStockException extends RuntimeException {
        public AestheticCareStockException(String msg) {
            super(msg);
        }
    }
}
