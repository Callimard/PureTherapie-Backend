package puretherapie.crm.data.product.aesthetic.care;

import lombok.*;
import puretherapie.crm.data.product.aesthetic.bundle.Bundle;

import javax.persistence.*;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AestheticCarePackage")
public class AestheticCarePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAestheticCarePackage", nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAestheticCare", nullable = false)
    private AestheticCare aestheticCare;

    @Column(name = "name", length = 35)
    private String name;

    @Column(name = "numberAestheticCare", nullable = false)
    private Integer numberAestheticCare;

    @ManyToMany
    @JoinTable(name = "AssociationBundleAestheticCarePackage", joinColumns = @JoinColumn(name = "idAestheticCarePackage"), inverseJoinColumns = @JoinColumn(name
            = "idBundle"))
    @ToString.Exclude
    private List<Bundle> bundleList;
}