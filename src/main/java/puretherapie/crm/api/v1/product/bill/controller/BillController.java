package puretherapie.crm.api.v1.product.bill.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.product.bill.controller.dto.BillDTO;
import puretherapie.crm.api.v1.product.bill.service.PaymentService;
import puretherapie.crm.api.v1.util.SimpleResponseDTO;
import puretherapie.crm.data.product.bill.Bill;
import puretherapie.crm.data.product.bill.MeansOfPayment;
import puretherapie.crm.data.product.bill.repository.BillRepository;
import puretherapie.crm.data.product.bill.repository.MeansOfPaymentRepository;

import java.util.List;

import static puretherapie.crm.WebSecurityConfiguration.FRONT_END_ORIGIN;
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

    // Variables.

    private final BillRepository billRepository;
    private final MeansOfPaymentRepository meansOfPaymentRepository;
    private final PaymentService paymentService;

    // Methods.

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
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

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping(MEANS_OF_PAYMENTS)
    public List<MeansOfPayment> getAllMeansOfPayments() {
        return meansOfPaymentRepository.findAll();
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PostMapping(PAY)
    public ResponseEntity<SimpleResponseDTO> payBill(@PathVariable(name = "idBill") int idBill,
                                                     @RequestParam(name = "amountToPaid") double amountToPaid,
                                                     @RequestParam(name = "idMeansOfPayment") int idMeansOfPayment) {
        return SimpleResponseDTO.generateResponse(paymentService.pay(idBill, amountToPaid, idMeansOfPayment));
    }

    @CrossOrigin(allowedHeaders = "*", origins = FRONT_END_ORIGIN, allowCredentials = "true")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @DeleteMapping(PAYMENTS + "/{idPayment}")
    public ResponseEntity<SimpleResponseDTO> cancelPayment(@PathVariable(name = "idPayment") int idPayment) {
        return SimpleResponseDTO.generateResponse(paymentService.cancelPayment(idPayment));
    }

}
