package puretherapie.crm.api.v1.kpi.service.kpi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.api.v1.person.client.service.ClientService;
import puretherapie.crm.data.appointment.Appointment;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundlePurchaseRepository;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;
import puretherapie.crm.tool.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static puretherapie.crm.api.v1.kpi.service.kpi.ClientPurchasesKPI.CLIENT_PURCHASES_KPI_NAME;

@Slf4j
@AllArgsConstructor
@Service(CLIENT_PURCHASES_KPI_NAME)
public class ClientPurchasesKPI extends ExecutableKPI {

    // Constants.

    public static final String CLIENT_PURCHASES_KPI_NAME = "ClientPurchasesKPI";

    // Variables.

    private final ClientService clientService;
    private final SessionPurchaseRepository sessionPurchaseRepository;
    private final BundlePurchaseRepository bundlePurchaseRepository;

    // Methods.

    @Override
    protected KPIRes executeFor(LocalDate begin, LocalDate end) {
        LocalDateTime beginDate = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endDate = LocalDateTime.of(end.plusDays(1), LocalTime.MIN);

        /* <productName, <<totalCounter, newClientCounter>, totalSalesPrice> */
        Map<String, Pair<Pair<Integer, Integer>, Double>> mapSessionPurchaseNumber = new HashMap<>();
        Map<String, Pair<Pair<Integer, Integer>, Double>> mapBundlePurchaseNumber = new HashMap<>();

        List<SessionPurchase> sessionPurchaseList = sessionPurchaseRepository.findByDateGreaterThanEqualAndDateLessThan(beginDate, endDate);
        List<BundlePurchase> bundlePurchases = bundlePurchaseRepository.findByDateGreaterThanEqualAndDateLessThan(beginDate, endDate);

        Pair<Pair<Integer, Integer>, Double> pairSessionPurchaseNumber = countSessionPurchase(mapSessionPurchaseNumber, sessionPurchaseList);
        Pair<Pair<Integer, Integer>, Double> pairBundlePurchaseNumber = countBundlePurchase(mapBundlePurchaseNumber, bundlePurchases);

        AllPurchases sessionAllPurchases =
                mapSessionPurchaseNumber.entrySet().stream().map(this::extractAllPurchases).reduce(this::mergeAllPurchases).orElse(null);
        AllPurchases bundleAllPurchases =
                mapBundlePurchaseNumber.entrySet().stream().map(this::extractAllPurchases).reduce(this::mergeAllPurchases).orElse(null);

        double totalSalesPrice = pairSessionPurchaseNumber.getSecond() + pairBundlePurchaseNumber.getSecond();
        int totalSaleCounter = pairSessionPurchaseNumber.getFirst().getFirst() + pairBundlePurchaseNumber.getFirst().getFirst();
        int totalNewClientSaleCounter = pairSessionPurchaseNumber.getFirst().getSecond() + pairBundlePurchaseNumber.getFirst().getSecond();
        double newClientRate = (double) totalNewClientSaleCounter / (double) totalSaleCounter;
        return new ClientPurchasesKPIRes(totalSalesPrice, newClientRate, sessionAllPurchases, bundleAllPurchases);
    }

    private AllPurchases extractAllPurchases(Map.Entry<String, Pair<Pair<Integer, Integer>, Double>> entry) {
        String acName = entry.getKey();
        Pair<Pair<Integer, Integer>, Double> pair = entry.getValue();
        Pair<Integer, Integer> pairCounter = pair.getFirst();
        int totalSalesCounter = pairCounter.getFirst();
        int newClientSalesCounter = pairCounter.getSecond();
        double acTotalSalesPrice = pair.getSecond();

        return new AllPurchases(acTotalSalesPrice, (double) newClientSalesCounter / (double) totalSalesCounter,
                                Collections.singletonList(new Purchase(acName, totalSalesCounter, newClientSalesCounter)));
    }

    private AllPurchases mergeAllPurchases(AllPurchases p1, AllPurchases p2) {
        ArrayList<Purchase> sessionPurchaseKPIS = new ArrayList<>();
        sessionPurchaseKPIS.addAll(p1.sessionPurchases);
        sessionPurchaseKPIS.addAll(p2.sessionPurchases);

        double totalSalesPrices = p1.totalSalesPrices + p2.totalSalesPrices;
        double newRate = p1.newClientSalesRate / (totalSalesPrices / p1.totalSalesPrices);
        double newRate2 = p2.newClientSalesRate / (totalSalesPrices / p2.totalSalesPrices);

        return new AllPurchases(totalSalesPrices,
                                newRate + newRate2,
                                sessionPurchaseKPIS);
    }

    private Pair<Pair<Integer, Integer>, Double> countSessionPurchase(Map<String, Pair<Pair<Integer, Integer>, Double>> mapSessionPurchaseNumber,
                                                                      List<SessionPurchase> sessionPurchaseList) {
        int sessionPurchaseCounter = 0;
        int sessionPurchaseNewClientCounter = 0;
        double totalPrice = 0.d;
        for (SessionPurchase sessionPurchase : sessionPurchaseList) {
            Pair<Pair<Integer, Integer>, Double> pair =
                    getOrInitPurchaseNumberPair(mapSessionPurchaseNumber, sessionPurchase.getAestheticCare().getName());

            sessionPurchaseCounter++;
            Pair<Integer, Integer> pairCounters = pair.getFirst();
            pairCounters.setFirst(pairCounters.getFirst() + 1);
            if (wasFirstAppointment(sessionPurchase.getClient(), sessionPurchase.getDate().toLocalDate())) {
                sessionPurchaseNewClientCounter++;
                pairCounters.setSecond(pairCounters.getSecond() + 1);
            }

            totalPrice += sessionPurchase.getBill().getPurchasePrice();
            pair.setSecond(pair.getSecond() + sessionPurchase.getBill().getPurchasePrice());
        }

        return new Pair<>(new Pair<>(sessionPurchaseCounter, sessionPurchaseNewClientCounter), totalPrice);
    }

    private Pair<Pair<Integer, Integer>, Double> countBundlePurchase(Map<String, Pair<Pair<Integer, Integer>, Double>> mapBundlePurchaseNumber,
                                                                     List<BundlePurchase> bundlePurchases) {
        int bundlePurchaseCounter = 0;
        int bundlePurchaseNewClientCounter = 0;
        double totalPrice = 0.d;
        for (BundlePurchase bundlePurchase : bundlePurchases) {
            Pair<Pair<Integer, Integer>, Double> pair = getOrInitPurchaseNumberPair(mapBundlePurchaseNumber, bundlePurchase.getBundle().getName());

            bundlePurchaseCounter++;
            Pair<Integer, Integer> pairCounters = pair.getFirst();
            pairCounters.setFirst(pairCounters.getFirst() + 1);
            if (wasFirstAppointment(bundlePurchase.getClient(), bundlePurchase.getDate().toLocalDate())) {
                bundlePurchaseNewClientCounter++;
                pairCounters.setSecond(pairCounters.getSecond() + 1);
            }

            totalPrice += bundlePurchase.getBill().getPurchasePrice();
            pair.setSecond(pair.getSecond() + bundlePurchase.getBill().getPurchasePrice());
        }

        return new Pair<>(new Pair<>(bundlePurchaseCounter, bundlePurchaseNewClientCounter), totalPrice);
    }

    private Pair<Pair<Integer, Integer>, Double> getOrInitPurchaseNumberPair(Map<String, Pair<Pair<Integer, Integer>, Double>> mapPurchaseNumber,
                                                                             String productName) {
        return mapPurchaseNumber.computeIfAbsent(productName, k -> new Pair<>(new Pair<>(0, 0), 0.d));
    }

    private boolean wasFirstAppointment(Client client, LocalDate purchaseDate) {
        Appointment clientFirstAppointment = clientService.getClientFirstAppointment(client);
        if (clientFirstAppointment != null) {
            return clientFirstAppointment.getDay().equals(purchaseDate);
        } else
            return true;
    }

    // Inner classes.

    public static class ClientPurchasesKPIRes extends KPIRes {
        public ClientPurchasesKPIRes(double totalSalesPrice, double totalNewClientRate,
                                     AllPurchases sessionAllPurchasesKPIS,
                                     AllPurchases bundleAllPurchasesKPIS) {
            super(CLIENT_PURCHASES_KPI_NAME, new ClientPurchasesPackage(totalSalesPrice, totalNewClientRate,
                                                                        sessionAllPurchasesKPIS,
                                                                        bundleAllPurchasesKPIS));
        }
    }

    /**
     * @param totalNewClientRate the rate in function of the number of sale (this is not the rate compare to the totalSalesPrice)
     */
    public static record ClientPurchasesPackage(double totalSalesPrice, double totalNewClientRate,
                                                AllPurchases sessionAllPurchasesKPIS,
                                                AllPurchases bundleAllPurchasesKPIS) {
    }

    public static record AllPurchases(double totalSalesPrices, double newClientSalesRate, List<Purchase> sessionPurchases) {
    }

    public static record Purchase(String productName, int nbSales, int nbNewClientSales) {
    }
}
