package puretherapie.crm.data.product.aesthetic.bundle;

import lombok.*;
import puretherapie.crm.api.v1.product.aesthetic.bundle.controller.dto.BundlePurchaseDTO;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.product.bill.Bill;

import javax.persistence.*;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BundlePurchase")
public class BundlePurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBundlePurchase", nullable = false)
    private Integer idBundlePurchase;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idClient", nullable = false)
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idBundle", nullable = false)
    private Bundle bundle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idBill", nullable = false)
    private Bill bill;

    @OneToMany(targetEntity = Stock.class, mappedBy = "bundlePurchase")
    @ToString.Exclude
    private List<Stock> stocks;

    public BundlePurchaseDTO transform() {
        return BundlePurchaseDTO.builder()
                .idBundlePurchase(idBundlePurchase)
                .client(client != null ? client.transform() : null)
                .bundle(bundle != null ? bundle.transform() : null)
                .bill(bill != null ? bill.transform() : null)
                .build();
    }
}