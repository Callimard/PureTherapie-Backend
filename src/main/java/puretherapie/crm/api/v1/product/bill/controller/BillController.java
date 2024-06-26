package puretherapie.crm.api.v1.product.bill.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.product.bill.controller.dto.BillDTO;
import puretherapie.crm.api.v1.product.bill.controller.dto.PaymentDTO;
import puretherapie.crm.api.v1.product.bill.controller.dto.PurchaseDTO;
import puretherapie.crm.api.v1.product.bill.service.PaymentService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.product.bill.Bill;
import puretherapie.crm.data.product.bill.MeansOfPayment;
import puretherapie.crm.data.product.bill.Payment;
import puretherapie.crm.data.product.bill.repository.BillRepository;
import puretherapie.crm.data.product.bill.repository.MeansOfPaymentRepository;

import java.util.ArrayList;
import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.product.bill.controller.BillController.BILL_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(BILL_URL)
public class BillController {

    // Constants.

    public static final String BILL_URL = API_V1_URL + "/bills";

    public static final String PAY = "/pay/{idBill}";
    public static final String PAY_URL = BILL_URL + PAY;

    public static final String PAYMENTS = "/payments";
    public static final String PAYMENTS_URL = BILL_URL + PAYMENTS;

    public static final String MEANS_OF_PAYMENTS = "/means_of_payments";

    private static final String MAKE_PAYMENT_TODAY = "/makePaymentToday";

    private static final String PURCHASES = "/purchases";

    // Variables.

    private final ClientRepository clientRepository;
    private final BillRepository billRepository;
    private final PaymentService paymentService;
    private final MeansOfPaymentRepository meansOfPaymentRepository;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(PURCHASES + "/{idClient}")
    public List<PurchaseDTO> getAllClientPurchases(@PathVariable(name = "idClient") int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);
        if (client != null) {
            return paymentService.getAllClientPurchases(client);
        } else
            throw new IllegalArgumentException("Unknown client id");
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping("/{idClient}" + MAKE_PAYMENT_TODAY)
    public boolean clientHasMakePaymentToday(@PathVariable(name = "idClient") int idClient) {
        return paymentService.hasDonePaymentToday(clientRepository.findByIdPerson(idClient));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping("/{idBill}")
    public ResponseEntity<BillDTO> getBill(@PathVariable(name = "idBill") int idBill) {
        try {
            Bill b = billRepository.findByIdBill(idBill);
            return ResponseEntity.ok(b != null ? b.transform() : null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(MEANS_OF_PAYMENTS)
    public List<MeansOfPayment> getAllMeansOfPayments() {
        return meansOfPaymentRepository.findAll();
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping(PAY)
    public ResponseEntity<SimpleResponseDTO> payBill(@PathVariable(name = "idBill") int idBill,
                                                     @RequestParam(name = "amountToPaid") double amountToPaid,
                                                     @RequestParam(name = "idMeansOfPayment") int idMeansOfPayment) {
        return SimpleResponseDTO.generateResponse(paymentService.pay(idBill, amountToPaid, idMeansOfPayment));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @DeleteMapping(PAYMENTS + "/{idPayment}")
    public ResponseEntity<SimpleResponseDTO> cancelPayment(@PathVariable(name = "idPayment") int idPayment) {
        return SimpleResponseDTO.generateResponse(paymentService.cancelPayment(idPayment));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(PAYMENTS + "/{idClient}")
    public List<PaymentDTO> getAllClientPayments(@PathVariable(name = "idClient") int idClient) {
        Client client = clientRepository.findByIdPerson(idClient);

        if (client != null) {
            List<Bill> bills = billRepository.findByClient(client);
            List<Payment> payments = new ArrayList<>();
            bills.forEach(bill -> payments.addAll(bill.getPayments()));
            return payments.stream().map(Payment::transform).toList();
        } else {
            throw new IllegalArgumentException("Unknown client");
        }
    }

}
