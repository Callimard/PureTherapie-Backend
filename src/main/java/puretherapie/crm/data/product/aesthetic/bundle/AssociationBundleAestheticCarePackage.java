package puretherapie.crm.data.product.aesthetic.bundle;

import lombok.*;
import puretherapie.crm.data.product.aesthetic.care.AestheticCarePackage;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AssociationBundleAestheticCarePackage")
public class AssociationBundleAestheticCarePackage {
    @EmbeddedId
    private AssociationBundleAestheticCarePackageId id;

    @MapsId("idBundle")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idBundle", nullable = false)
    @ToString.Exclude
    private Bundle bundle;

    @MapsId("idAestheticCarePackage")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idAestheticCarePackage", nullable = false)
    @ToString.Exclude
    private AestheticCarePackage aestheticCarePackage;
}