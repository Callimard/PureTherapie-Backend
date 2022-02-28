package puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCarePackageDTO;
import puretherapie.crm.data.product.aesthetic.bundle.Bundle;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BundleDTO {

    private int idBundle;
    private String name;
    private double price;
    private boolean active;
    private List<AestheticCarePackageDTO> aestheticCarePackageList;

    public Bundle transform() {
        return Bundle.builder()
                .idBundle(idBundle)
                .name(name)
                .price(price)
                .active(active)
                .aestheticCarePackages(aestheticCarePackageList.stream().map(AestheticCarePackageDTO::transform).toList())
                .build();
    }

}
