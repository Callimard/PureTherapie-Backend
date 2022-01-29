package puretherapie.crm.api.v1.product.bill.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BillDTO {

    private int idBill;
    private double basePrice;
    private double purchasePrice;
    private String creationDate;
    private ClientDTO client;
    private PaymentTypeDTO paymentType;
    private List<PaymentDTO> payments;
}
