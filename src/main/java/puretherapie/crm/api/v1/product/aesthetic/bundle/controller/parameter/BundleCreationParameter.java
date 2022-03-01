package puretherapie.crm.api.v1.product.aesthetic.bundle.controller.parameter;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BundleCreationParameter {

    private String name;
    private double price;
    private List<List<Integer>> mapACStock;

}
