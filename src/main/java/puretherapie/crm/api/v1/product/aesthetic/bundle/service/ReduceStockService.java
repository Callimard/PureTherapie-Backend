package puretherapie.crm.api.v1.product.aesthetic.bundle.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.api.v1.SimpleService;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;
import puretherapie.crm.data.product.aesthetic.bundle.repository.StockRepository;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class ReduceStockService extends SimpleService {

    // Constants.

    public static final String REDUCE_STOCK_SUCCESS = "reduce_stock_success";
    public static final String REDUCE_STOCK_FAIL = "reduce_stock_fail";

    public static final String STOCK_NOT_FOUND_ERROR = "not_found_stock_error";
    public static final String REDUCE_QUANTITY_ERROR = "reduce_quantity_error";

    // Variables.

    private final StockRepository stockRepository;

    // Methods.

    @Transactional(propagation = Propagation.SUPPORTS)
    public Map<String, Object> reduceStock(int idStock, int reduceQuantity) {
        try {
            Stock stock = verifyStock(idStock);
            updateStock(stock, reduceQuantity);
            return generateSuccessRes();
        } catch (Exception e) {
            log.debug("Fail to reduce stock, error message: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return generateErrorRes(e);
        }
    }

    private Stock verifyStock(int idStock) {
        Stock stock = stockRepository.findByIdStock(idStock);
        if (stock == null)
            throw new ReduceStockException("Stock not found", generateError(STOCK_NOT_FOUND_ERROR, "Stock not found"));

        return stock;
    }

    private void updateStock(Stock stock, int reduceQuantity) {
        try {
            stock.reduce(reduceQuantity);
            stockRepository.save(stock);
            log.debug("Update stock {}", stock);
        } catch (IllegalArgumentException e) {
            log.debug("Fail stock.reduce, e.msg = {}", e.getMessage());
            throw new ReduceStockException("Error with reduce quantity", generateError(REDUCE_QUANTITY_ERROR, "Reduce quantity error"));
        }
    }

    // SimpleService methods.

    @Override
    public String getSuccessTag() {
        return REDUCE_STOCK_SUCCESS;
    }

    @Override
    public String getFailTag() {
        return REDUCE_STOCK_FAIL;
    }

    // Exceptions.

    private static class ReduceStockException extends SimpleService.ServiceException {
        public ReduceStockException(String message, Map<String, String> errors) {
            super(message, errors);
        }
    }
}
