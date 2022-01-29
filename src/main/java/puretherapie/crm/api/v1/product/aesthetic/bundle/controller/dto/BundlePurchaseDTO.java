package puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.person.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.product.bill.controller.dto.BillDTO;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BundlePurchaseDTO {

    private int idBundlePurchase;
    private ClientDTO client;
    private BundleDTO bundle;
    private BillDTO bill;

}
