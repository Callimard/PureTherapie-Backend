package puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCareDTO;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {

    private int idStock;
    private int remainingQuantity;
    private AestheticCareDTO aestheticCare;
    private BundlePurchaseDTO bundlePurchase;

}
