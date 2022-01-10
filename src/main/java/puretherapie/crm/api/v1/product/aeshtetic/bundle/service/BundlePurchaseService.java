package puretherapie.crm.api.v1.product.aeshtetic.bundle.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.product.aesthetic.bundle.Bundle;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundlePurchaseRepository;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundleRepository;
import puretherapie.crm.data.product.aesthetic.bundle.repository.StockRepository;
import puretherapie.crm.data.product.aesthetic.care.AestheticCarePackage;
import puretherapie.crm.data.product.bill.Bill;
import puretherapie.crm.data.product.bill.PaymentType;
import puretherapie.crm.data.product.bill.repository.BillRepository;
import puretherapie.crm.data.product.bill.repository.PaymentTypeRepository;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class BundlePurchaseService extends SimpleService {

    // Constants.

    public static final String BUNDLE_PURCHASE_SUCCESS = "bundle_purchase_success";
    public static final String BUNDLE_PURCHASE_FAIL = "bundle_purchase_fail";

    public static final String CLIENT_NOT_FOUND_ERROR = "client_not_found_error";
    public static final String BUNDLE_NOT_FOUND_ERROR = "bundle_not_found_error";
    public static final String PAYMENT_TYPE_NOT_FOUND = "payment_type_not_found_error";
    public static final String EMPTY_BUNDLE_ERROR = "empty_bundle_error";

    // Variables.

    private final ClientRepository clientRepository;
    private final BundleRepository bundleRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final BillRepository billRepository;
    private final BundlePurchaseRepository bundlePurchaseRepository;
    private final StockRepository stockRepository;

    // Methods.

    /**
     * @param idClient                id client
     * @param idBundle                id bundle
     * @param acPackageCustomizations list of ac package customization
     * @param customPrice             custom Price (not take in account if less than 0)
     * @param idPaymentType           id payment type
     *
     * @return res of the try to purchase bundle.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> purchaseBundle(int idClient, int idBundle, Set<ACPackageCustomization> acPackageCustomizations, double customPrice,
                                              int idPaymentType) {
        try {
            Client client = verifyClient(idClient);
            Bundle bundle = verifyBundle(idBundle);
            PaymentType paymentType = verifyPaymentType(idPaymentType);
            Bill bill = saveBill(client, paymentType, bundle.getPrice(), customPrice);
            BundlePurchase bundlePurchase = saveBundlePurchase(client, bundle, bill);
            saveAllStocks(bundle, bundlePurchase, acPackageCustomizations);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to purchase a bundle, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private Client verifyClient(int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client == null)
            throw new BundlePurchaseException("Client not found", generateError(CLIENT_NOT_FOUND_ERROR, "Client id not found"));
        return client;
    }

    private Bundle verifyBundle(int idBundle) {
        Bundle bundle = bundleRepository.findByIdBundle(idBundle);
        verifyNotNullBundle(bundle);
        verifyNotEmptyBundle(bundle);

        return bundle;
    }

    private void verifyNotNullBundle(Bundle bundle) {
        if (bundle == null)
            throw new BundlePurchaseException("Bundle not found", generateError(BUNDLE_NOT_FOUND_ERROR, "Bundle not found"));
    }

    private void verifyNotEmptyBundle(Bundle bundle) {
        if (bundle.getAestheticCarePackages() == null || bundle.getAestheticCarePackages().isEmpty())
            throw new BundlePurchaseException("Empty bundle", generateError(EMPTY_BUNDLE_ERROR, "Empty bundle"));
    }

    private PaymentType verifyPaymentType(int idPaymentType) {
        PaymentType paymentType = paymentTypeRepository.findByIdPaymentType(idPaymentType);
        if (paymentType == null)
            throw new BundlePurchaseException("PaymentType not found", generateError(PAYMENT_TYPE_NOT_FOUND, "PaymentType not found"));

        return paymentType;
    }

    /**
     * @param client      the client
     * @param paymentType the payment type
     * @param basePrice   the base price
     * @param customPrice the custom price (not take in account if less than 0)
     *
     * @return the built bill
     */
    private Bill saveBill(Client client, PaymentType paymentType, double basePrice, double customPrice) {
        Bill bill = buildBill(client, paymentType, basePrice, customPrice);
        bill = billRepository.save(bill);
        log.debug("Save bill {}", bill);
        return bill;
    }

    private Bill buildBill(Client client, PaymentType paymentType, double basePrice, double customPrice) {
        return Bill.builder()
                .client(client)
                .paymentType(paymentType)
                .basePrice(basePrice)
                .purchasePrice(customPrice < 0 ? basePrice : customPrice)
                .creationDate(OffsetDateTime.now())
                .build();
    }

    private BundlePurchase saveBundlePurchase(Client client, Bundle bundle, Bill bill) {
        BundlePurchase bundlePurchase = buildBundle(client, bundle, bill);
        bundlePurchase = bundlePurchaseRepository.save(bundlePurchase);
        log.debug("Save BundlePurchase {}", bundlePurchase);
        return bundlePurchase;
    }

    private BundlePurchase buildBundle(Client client, Bundle bundle, Bill bill) {
        return BundlePurchase.builder()
                .client(client)
                .bundle(bundle)
                .bill(bill)
                .build();
    }

    private void saveAllStocks(Bundle bundle, BundlePurchase bundlePurchase, Set<ACPackageCustomization> acPackageCustomizations) {
        Map<Integer, Stock> mapStock = new HashMap<>();
        for (AestheticCarePackage acPackage : bundle.getAestheticCarePackages()) {
            Stock stock = buildStock(bundlePurchase, acPackage);
            mapStock.put(acPackage.getIdAestheticCarePackage(), stock);
            log.debug("Build stock {}", stock);
        }

        if (acPackageCustomizations != null) {
            for (ACPackageCustomization customization : acPackageCustomizations) {
                Stock stock = mapStock.get(customization.idAestheticCarePackage());
                if (stock != null) {
                    stock.setRemainingQuantity(customization.customStock() > 0 ? customization.customStock : 0);
                    log.debug("Custom stock {}", stock);
                } else
                    log.debug("Customisation to aesthetic care package not in the bundle");
            }
        }

        List<Stock> saved = stockRepository.saveAll(mapStock.values());
        log.debug("Save all stocks {}", saved);
    }

    private Stock buildStock(BundlePurchase bundlePurchase, AestheticCarePackage acPackage) {
        return Stock.builder()
                .bundlePurchase(bundlePurchase)
                .aestheticCare(acPackage.getAestheticCare())
                .remainingQuantity(acPackage.getNumberAestheticCare())
                .build();
    }

    // SimpleService methods.

    @Override
    public String getSuccessTag() {
        return BUNDLE_PURCHASE_SUCCESS;
    }

    @Override
    public String getFailTag() {
        return BUNDLE_PURCHASE_FAIL;
    }

    // Inner class.

    public static record ACPackageCustomization(int idAestheticCarePackage, int customStock) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ACPackageCustomization that)) return false;
            return idAestheticCarePackage == that.idAestheticCarePackage;
        }

        @Override
        public int hashCode() {
            return Objects.hash(idAestheticCarePackage);
        }
    }

    // Exception.

    private static class BundlePurchaseException extends SimpleService.ServiceException {
        public BundlePurchaseException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }

}
