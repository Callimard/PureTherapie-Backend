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

    /**
     * @return true if remaining quantity >= 1.
     */
    public boolean hasRemainingQuantity() {
        return remainingQuantity >= 1;
    }

    /**
     * Reduce the quantity of remaining quantity
     *
     * @param reduceQuantity the quantity to reduce
     *
     * @throws IllegalArgumentException if reduce quantity is negative, if stock is already 0 or if the reduction make a negative stock
     */
    public void reduce(int reduceQuantity) {
        if (reduceQuantity <= 0 || getRemainingQuantity() == 0 || (getRemainingQuantity() - reduceQuantity) < 0)
            throw new IllegalArgumentException(
                    "Wrong reduce quantity -> rq = %s, stock = %s, stock - rq = %s".formatted(reduceQuantity, getRemainingQuantity(),
                                                                                              getRemainingQuantity() - reduceQuantity));
        else
            setRemainingQuantity(getRemainingQuantity() - reduceQuantity);
    }
}