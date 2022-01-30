package puretherapie.crm.api.v1.product.bill.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.product.bill.Bill;
import puretherapie.crm.data.product.bill.MeansOfPayment;
import puretherapie.crm.data.product.bill.Payment;
import puretherapie.crm.data.product.bill.repository.BillRepository;
import puretherapie.crm.data.product.bill.repository.MeansOfPaymentRepository;
import puretherapie.crm.data.product.bill.repository.PaymentRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class PaymentService {

    // Constants.

    public static final String PAYMENT_NOT_FOUND_ERROR = "payment_not_found_error";
    public static final String BILL_NOT_FOUND_ERROR = "bill_not_found_error";
    public static final String AMOUNT_TO_PAID_NEGATIVE_ERROR = "amount_to_paid_negative_error";
    public static final String MEANS_OF_PAYMENT_NOT_FOUND_ERROR = "means_of_payment_not_found_error";
    public static final String TOO_MUCH_PAID_ERROR = "too_much_paid_error";

    // Variables.

    private final PaymentRepository paymentRepository;
    private final MeansOfPaymentRepository meansOfPaymentRepository;
    private final BillRepository billRepository;

    // Methods.

    public SimpleResponseDTO cancelPayment(int idPayment) {
        try {
            Payment payment = verifyPayment(idPayment);
            if (!payment.isCanceled()) {
                payment.setCanceled(true);
                updatePayment(payment);
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
            verifyAmountPaid(amountToPaid);
            MeansOfPayment meansOfPayment = verifyMeansOfPayment(idMeansOfPayment);
            verifyNotTooMuch(bill, amountToPaid);
            Payment payment = createPayment(amountToPaid, meansOfPayment, bill);
            savePayment(payment);
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

    private void verifyAmountPaid(double amountToPaid) {
        if (amountToPaid < 0.0d)
            throw new PaymentServiceException(AMOUNT_TO_PAID_NEGATIVE_ERROR);
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
                .paymentDate(OffsetDateTime.now())
                .meansOfPayment(meansOfPayment)
                .bill(bill)
                .build();
    }

    private void savePayment(Payment payment) {
        payment = paymentRepository.save(payment);
        log.info("Save payment = {}", payment);
    }

    // Exceptions.

    private static class PaymentServiceException extends RuntimeException {
        public PaymentServiceException(String message) {
            super(message);
        }
    }

}
