package puretherapie.crm.data.product.aesthetic.bundle;

import lombok.*;
import puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto.BundleDTO;
import puretherapie.crm.data.product.aesthetic.care.AestheticCarePackage;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Bundle")
public class Bundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBundle", nullable = false)
    private Integer idBundle;

    @Column(name = "name", nullable = false, length = 35)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @ManyToMany
    @JoinTable(name = "AssociationBundleAestheticCarePackage", joinColumns = @JoinColumn(name = "idBundle"), inverseJoinColumns = @JoinColumn(name
            = "idAestheticCarePackage"))
    @ToString.Exclude
    private List<AestheticCarePackage> aestheticCarePackages;

    public BundleDTO transform() {
        return BundleDTO.builder()
                .idBundle(idBundle)
                .name(name)
                .price(price)
                .aestheticCarePackageList(
                        aestheticCarePackages != null ? aestheticCarePackages.stream().map(AestheticCarePackage::transform).toList() :
                                Collections.emptyList())
                .build();
    }
}