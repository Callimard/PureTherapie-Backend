package puretherapie.crm.api.v1.product.aesthetic.bundle.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.data.product.aesthetic.bundle.Stock;
import puretherapie.crm.data.product.aesthetic.bundle.repository.StockRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static puretherapie.crm.api.v1.product.aesthetic.bundle.service.ReduceStockService.REDUCE_QUANTITY_ERROR;
import static puretherapie.crm.api.v1.product.aesthetic.bundle.service.ReduceStockService.STOCK_NOT_FOUND_ERROR;

@SpringBootTest
@DisplayName("ReduceStockService tests")
public class ReduceStockServiceTest {

    @Autowired
    private ReduceStockService rss;

    @BeforeEach
    void setUp() {
        prepareStockRepository();
    }

    @Nested
    @DisplayName("Reduce stock tests")
    class ReduceStock {

        @Test
        @DisplayName("Test with stock not found fail")
        void testNotFoundStock() {
            Map<String, Object> res = rss.reduceStock(-1, 1);
            verifyFail(res);
            verifyFailType(res, STOCK_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("Test with reduce fail fail")
        void testWithZeroStock() {
            prepareStockReduceFail();

            Map<String, Object> res = rss.reduceStock(STOCK_ID, 1);
            verifyFail(res);
            verifyFailType(res, REDUCE_QUANTITY_ERROR);
        }

        @Test
        @DisplayName("Test with reduce success success")
        void testWithOneOne() {
            Map<String, Object> res = rss.reduceStock(STOCK_ID, 1);
            verifySuccess(res);
        }

    }

    private void verifySuccess(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(rss.getSuccessTag());
    }

    private void verifyFail(Map<String, Object> res) {
        assertThat(res).isNotNull().containsKey(rss.getFailTag());
    }

    void verifyFailType(Map<String, Object> res, String expectedKey) {
        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) res.get(rss.getFailTag());
        assertThat(errors).isNotNull().containsKey(expectedKey);
    }

    // Context.

    @MockBean
    private StockRepository mockStockRepository;
    @Mock
    private Stock mockStock;
    private static final int STOCK_ID = 509;

    private void prepareStockRepository() {
        given(mockStockRepository.findByIdStock(STOCK_ID)).willReturn(mockStock);
    }

    private void prepareStockReduceFail() {
        doThrow(new IllegalArgumentException()).when(mockStock).reduce(anyInt());
    }

}
