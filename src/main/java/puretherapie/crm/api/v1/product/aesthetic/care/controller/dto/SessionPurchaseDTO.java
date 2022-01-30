package puretherapie.crm.api.v1.product.aesthetic.care.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.product.bill.controller.dto.BillDTO;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SessionPurchaseDTO {

    private int idSessionPurchase;
    private boolean used;
    private AestheticCareDTO aestheticCare;
    private ClientDTO client;
    private BillDTO bill;

}
