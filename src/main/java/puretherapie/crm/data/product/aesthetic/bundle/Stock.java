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
    @Column(name = "idSotck", nullable = false)
    private Integer id;

    @Column(name = "remainingQuantity", nullable = false)
    private Integer remainingQuantity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idAestheticCare", nullable = false)
    private AestheticCare idAestheticCare;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idBundlePurchase", nullable = false)
    private BundlePurchase idBundlePurchase;
}