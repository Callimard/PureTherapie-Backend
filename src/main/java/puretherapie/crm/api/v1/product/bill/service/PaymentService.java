package puretherapie.crm.api.v1.product.bill.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.historical.service.HistoricalCreationService;
import puretherapie.crm.api.v1.product.bill.controller.dto.PurchaseDTO;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundlePurchaseRepository;
import puretherapie.crm.data.product.aesthetic.care.SessionPurchase;
import puretherapie.crm.data.product.aesthetic.care.repository.SessionPurchaseRepository;
import puretherapie.crm.data.product.bill.Bill;
import puretherapie.crm.data.product.bill.MeansOfPayment;
import puretherapie.crm.data.product.bill.Payment;
import puretherapie.crm.data.product.bill.repository.BillRepository;
import puretherapie.crm.data.product.bill.repository.MeansOfPaymentRepository;
import puretherapie.crm.data.product.bill.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static puretherapie.crm.data.historical.HistoricalLevel.BOSS_LEVEL;

@Slf4j
@AllArgsConstructor
@Service
public class PaymentService {

    // Constants.

    public static final String PAYMENT_DONE_TITLE = "Paiment effectué";
    public static final String PAYMENT_DONE_TEXT = "Le client %s a effectué un paiement de %s €";

    public static final String PAYMENT_CANCELED_TITLE = "Paiement annulé";
    public static final String PAYMENT_CANCELED_TEXT = "Un paiement de %s € a été annulé pour le client %s";

    public static final String PAYMENT_NOT_FOUND_ERROR = "payment_not_found_error";
    public static final String BILL_NOT_FOUND_ERROR = "bill_not_found_error";
    public static final String AMOUNT_TO_PAID_NEGATIVE_ERROR = "amount_to_paid_negative_error";
    public static final String GROUPON_AMOUNT_PAYMENT_NOT_EQUAL_TO_ZERO = "groupon_amount_payment_not_equal_to_zero";
    public static final String MEANS_OF_PAYMENT_NOT_FOUND_ERROR = "means_of_payment_not_found_error";
    public static final String TOO_MUCH_PAID_ERROR = "too_much_paid_error";

    // Variables.

    private final PaymentRepository paymentRepository;
    private final MeansOfPaymentRepository meansOfPaymentRepository;
    private final BillRepository billRepository;
    private final HistoricalCreationService historicalCreationService;
    private final BundlePurchaseRepository bundlePurchaseRepository;
    private final SessionPurchaseRepository sessionPurchaseRepository;

    // Methods.

    public List<PurchaseDTO> getAllClientPurchases(Client client) {
        List<BundlePurchase> clientBundlePurchases = bundlePurchaseRepository.findByClient(client);
        List<SessionPurchase> clientSessionPurchases = sessionPurchaseRepository.findByClient(client);

        List<PurchaseDTO> allPurchases = new ArrayList<>();

        clientBundlePurchases.forEach(bundlePurchase -> {
            PurchaseDTO purchase = bundlePurchase.toPurchase();
            purchase.setTotallyPaid(!billNotTotallyPaid(bundlePurchase.getBill()));
            allPurchases.add(purchase);
        });

        clientSessionPurchases.forEach(sessionPurchase -> {
            PurchaseDTO purchase = sessionPurchase.toPurchase();
            purchase.setTotallyPaid(!billNotTotallyPaid(sessionPurchase.getBill()));
            allPurchases.add(purchase);
        });

        return allPurchases;
    }

    public double totalLeftToPay(Client client) {
        List<Bill> bills = billRepository.findByClient(client);

        double leftToPaid = 0.0;
        for (Bill bill : bills)
            leftToPaid += bill.getPurchasePrice() - amountPaid(bill);

        return leftToPaid;
    }


    public double amountPaid(Bill bill) {
        if (billNotTotallyPaid(bill)) {
            double amountPaid = 0.0d;
            for (Payment payment : bill.getPayments()) {
                if (!payment.isCanceled()) {
                    if (payment.getMeansOfPayment().isGrouponPayment()) {
                        return bill.getPurchasePrice();
                    } else {
                        amountPaid += payment.getAmountPaid();
                    }
                }
            }
            return amountPaid;
        } else
            return bill.getPurchasePrice();
    }


    /**
     * @param client the client
     *
     * @return true if the client has totally paid all his {@link Bill}.
     */
    public boolean hasRemainingPayment(Client client) {
        List<Bill> bills = billRepository.findByClient(client);

        for (Bill bill : bills)
            if (billNotTotallyPaid(bill))
                return true;

        return false;
    }

    public List<Bill> allBills(Client client) {
        return billRepository.findByClient(client);
    }

    public List<Bill> allBillNotTotallyPaid(Client client) {
        List<Bill> bills = billRepository.findByClient(client);
        return bills.stream().filter(this::billNotTotallyPaid).toList();
    }

    public boolean hasDonePaymentToday(Client client) {
        return !paymentDoneToday(client).isEmpty();
    }

    public List<Payment> paymentDoneToday(Client client) {
        List<Payment> paymentDoneToday = new ArrayList<>();
        for (Bill bill : allBills(client)) {
            List<Payment> billPayments = bill.getPayments();
            paymentDoneToday.addAll(
                    billPayments.stream().filter(payment -> LocalDateTime.now().toLocalDate().equals(payment.getPaymentDate().toLocalDate()))
                            .toList());
        }

        return paymentDoneToday;
    }

    public boolean billNotTotallyPaid(Bill bill) {
        if (bill.getPayments() != null) {
            double amountPaid = 0.d;
            for (Payment payment : bill.getPayments()) {
                if (!payment.isCanceled()) {
                    if (payment.getMeansOfPayment().isGrouponPayment()) {
                        return false;
                    } else {
                        amountPaid += payment.getAmountPaid();
                    }
                }
            }
            return amountPaid < bill.getPurchasePrice();
        } else
            return false;
    }

    public SimpleResponseDTO cancelPayment(int idPayment) {
        try {
            Payment payment = verifyPayment(idPayment);
            if (!payment.isCanceled()) {
                payment.setCanceled(true);
                updatePayment(payment);
                notifyPaymentCanceled(payment.getBill().getClient(), payment.getAmountPaid());
            } else
                log.info("Payment already canceled");
            return SimpleResponseDTO.generateSuccess("Success to cancel payment");
        } catch (Exception e) {
            log.error("Fail to cancel payment, Err msg = {}", e.getMessage());
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private void updatePayment(Payment payment) {
        payment = paymentRepository.save(payment);
        log.info("Payment update, updated payment = {}", payment);
    }

    private Payment verifyPayment(int idPayment) {
        Payment payment = paymentRepository.findByIdPayment(idPayment);
        if (payment == null)
            throw new PaymentServiceException(PAYMENT_NOT_FOUND_ERROR);

        return payment;
    }

    public SimpleResponseDTO pay(int idBill, double amountToPaid, int idMeansOfPayment) {
        try {
            Bill bill = verifyBill(idBill);
            MeansOfPayment meansOfPayment = verifyMeansOfPayment(idMeansOfPayment);
            verifyAmountPaid(amountToPaid, meansOfPayment);

            if (meansOfPayment.isGrouponPayment())
                amountToPaid = bill.getPurchasePrice();

            verifyNotTooMuch(bill, amountToPaid);
            Payment payment = createPayment(amountToPaid, meansOfPayment, bill);
            savePayment(payment);
            notifyPaymentDone(bill.getClient(), payment.getAmountPaid());
            return SimpleResponseDTO.generateSuccess("Success to pay");
        } catch (Exception e) {
            log.error("Fail to paid, Error = {}", e.getMessage());
            return SimpleResponseDTO.generateFail(e.getMessage());
        }
    }

    private Bill verifyBill(int idBill) {
        Bill bill = billRepository.findByIdBill(idBill);
        if (bill == null)
            throw new PaymentServiceException(BILL_NOT_FOUND_ERROR);

        return bill;
    }

    private void verifyAmountPaid(double amountToPaid, MeansOfPayment meansOfPayment) {
        if (meansOfPayment.isGrouponPayment()) {
            if (amountToPaid != 0.0d)
                throw new PaymentServiceException(GROUPON_AMOUNT_PAYMENT_NOT_EQUAL_TO_ZERO);
        } else {
            if (amountToPaid <= 0.0d)
                throw new PaymentServiceException(AMOUNT_TO_PAID_NEGATIVE_ERROR);
        }
    }

    private MeansOfPayment verifyMeansOfPayment(int idMeansOfPayment) {
        MeansOfPayment meansOfPayment = meansOfPaymentRepository.findByIdMeansOfPayment(idMeansOfPayment);
        if (meansOfPayment == null)
            throw new PaymentServiceException(MEANS_OF_PAYMENT_NOT_FOUND_ERROR);

        return meansOfPayment;
    }

    private void verifyNotTooMuch(Bill bill, double amountToPaid) {
        List<Payment> billPayments = bill.getPayments();
        if (billPayments != null) {
            double amountAlreadyPaid = 0.0d;
            for (Payment payment : billPayments) {
                if (!payment.isCanceled())
                    amountAlreadyPaid += payment.getAmountPaid();
            }

            if ((amountAlreadyPaid + amountToPaid) > bill.getPurchasePrice())
                throw new PaymentServiceException(TOO_MUCH_PAID_ERROR);
        }
    }

    private Payment createPayment(double amountPaid, MeansOfPayment meansOfPayment, Bill bill) {
        return Payment.builder()
                .amountPaid(amountPaid)
                .paymentDate(LocalDateTime.now())
                .meansOfPayment(meansOfPayment)
                .bill(bill)
                .build();
    }

    private void savePayment(Payment payment) {
        payment = paymentRepository.save(payment);
        log.info("Save payment = {}", payment);
    }

    private void notifyPaymentDone(Client client, double amount) {
        boolean success = historicalCreationService.createHistorical(PAYMENT_DONE_TITLE,
                                                                     PAYMENT_DONE_TEXT.formatted(client.simplyIdentifier(), amount),
                                                                     BOSS_LEVEL, false);
        if (!success)
            log.error("Fail to create payment done notification");
    }

    private void notifyPaymentCanceled(Client client, double amount) {
        boolean success = historicalCreationService.createHistorical(PAYMENT_CANCELED_TITLE,
                                                                     PAYMENT_CANCELED_TEXT.formatted(amount, client.simplyIdentifier()),
                                                                     BOSS_LEVEL, true);
        if (!success)
            log.error("Fail to create payment canceled notification");
    }

    // Exceptions.

    private static class PaymentServiceException extends RuntimeException {
        public PaymentServiceException(String message) {
            super(message);
        }
    }

}
