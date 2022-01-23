package puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto;

import lombok.*;
import puretherapie.crm.api.v1.product.aesthetic.care.controller.dto.AestheticCarePackageDTO;

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
    private List<AestheticCarePackageDTO> aestheticCareDTOList;

}
