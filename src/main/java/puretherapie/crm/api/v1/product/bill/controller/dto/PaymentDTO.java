package puretherapie.crm.api.v1.product.bill.controller.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private int idPayment;
    private double amountPaid;
    private String paymentDate;
    private BillDTO bill;

}
