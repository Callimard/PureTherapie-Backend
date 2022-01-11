package puretherapie.crm.data.product.aesthetic.bundle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {
}