package puretherapie.crm.data.product.aesthetic.bundle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {

    Stock findByIdStock(int idStock);

    Stock findByBundlePurchaseAndAestheticCare(BundlePurchase bundlePurchase, AestheticCare aestheticCare);

}