package puretherapie.crm.data.product.aesthetic.bundle;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto.BundlePurchaseDTO;
import puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto.StockDTO;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;

import javax.persistence.*;
import java.util.Objects;

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
    private int remainingQuantity;

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

    public boolean isAssociatedTo(AestheticCare aestheticCare) {
        return Objects.equals(this.aestheticCare.getIdAestheticCare(), aestheticCare.getIdAestheticCare());
    }

    public StockDTO transform() {
        return StockDTO.builder()
                .idStock(idStock)
                .remainingQuantity(remainingQuantity)
                .aestheticCare(aestheticCare != null ? aestheticCare.transform() : null)
                .bundlePurchase(bundlePurchase != null ? bundlePurchase.transform() : null)
                .build();
    }

    public StockDTO transform(BundlePurchaseDTO bundlePurchase) {
        return StockDTO.builder()
                .idStock(idStock)
                .remainingQuantity(remainingQuantity)
                .aestheticCare(aestheticCare != null ? aestheticCare.transform() : null)
                .bundlePurchase(bundlePurchase)
                .build();
    }
}