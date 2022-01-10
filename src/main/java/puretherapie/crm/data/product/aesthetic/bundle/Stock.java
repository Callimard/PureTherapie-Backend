package puretherapie.crm.data.product.aesthetic.bundle;

import lombok.*;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;

import javax.persistence.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStock", nullable = false)
    private Integer idStock;

    @Column(name = "remainingQuantity", nullable = false)
    private Integer remainingQuantity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAestheticCare", nullable = false)
    private AestheticCare aestheticCare;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idBundlePurchase", nullable = false)
    private BundlePurchase bundlePurchase;
}