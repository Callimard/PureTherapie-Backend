package puretherapie.crm.api.v1.product.aesthetic.bundle.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.product.aesthetic.bundle.BundlePurchase;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;
import puretherapie.crm.data.product.aesthetic.bundle.repository.BundlePurchaseRepository;
import puretherapie.crm.data.product.aesthetic.bundle.repository.StockRepository;

import java.util.Collections;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class StockService {

    // Constants.

    public static final String STOCK_NOT_FOUND = "stock_not_found";
    public static final String BUNDLE_PURCHASE_NOT_FOUND = "bundle_purchase_not_found";
    public static final String UPDATED_REMAINING_STOCK_NEGATIVE = "updated_remaining_stock_negative";

    // Variables.

    private final BundlePurchaseRepository bundlePurchaseRepository;
    private final StockRepository stockRepository;

    // Methods

    /**
     * Update the remaining quantity of the stock
     *
     * @param idStock                  the stock id
     * @param updatedRemainingQuantity the updated remaining quantity of the stock
     *
     * @return true if the stock has been updated, else false.
     */
    public boolean updateStock(int idStock, int updatedRemainingQuantity) {
        try {
            Stock stock = verifyStock(idStock);
            verifyRemainingStock(updatedRemainingQuantity);
            stock.setRemainingQuantity(updatedRemainingQuantity);
            updateStock(stock);
            return true;
        } catch (Exception e) {
            log.error("Fail to update stock, Error msg = {}", e.getMessage());
            return false;
        }
    }

    private Stock verifyStock(int idStock) {
        Stock stock = stockRepository.findByIdStock(idStock);
        if (stock == null)
            throw new StockServiceException(STOCK_NOT_FOUND);

        return stock;
    }

    private void verifyRemainingStock(int updatedRemainingQuantity) {
        if (updatedRemainingQuantity < 0)
            throw new StockServiceException(UPDATED_REMAINING_STOCK_NEGATIVE);
    }

    private void updateStock(Stock stock) {
        stock = stockRepository.save(stock);
        log.info("Update stock, updated stock = {}", stock);
    }

    public List<Stock> getAllStocks(int idBundlePurchase) {
        try {
            BundlePurchase bundlePurchase = verifyBundlePurchase(idBundlePurchase);
            return stockRepository.findByBundlePurchase(bundlePurchase);
        } catch (Exception e) {
            log.error("Fail to get all stocks of the bundle purchase, Error msg = {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private BundlePurchase verifyBundlePurchase(int idBundlePurchase) {
        BundlePurchase bundlePurchase = bundlePurchaseRepository.findByIdBundlePurchase(idBundlePurchase);
        if (bundlePurchase == null)
            throw new StockServiceException(BUNDLE_PURCHASE_NOT_FOUND);

        return bundlePurchase;
    }

    // Exceptions.

    private static class StockServiceException extends RuntimeException {
        public StockServiceException(String message) {
            super(message);
        }
    }

}
