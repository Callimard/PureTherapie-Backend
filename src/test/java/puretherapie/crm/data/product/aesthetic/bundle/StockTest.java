package puretherapie.crm.data.product.aesthetic.bundle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Stock tests")
public class StockTest {

    @Nested
    @DisplayName("Has Remaining Quantity tests")
    class HasRemainingQuantity {

        @Test
        @DisplayName("Test with 0 remaining stock returns false")
        void testWithZeroRemainingStock() {
            Stock s = buildStock(0);

            boolean hasRemainingQuantity = s.hasRemainingQuantity();
            assertThat(hasRemainingQuantity).isFalse();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 456, 9871, 67432, 564, 6454, 654, 6})
        @DisplayName("Test with remaining stock >= 1 returns true")
        void testWithGreaterOrEqualToOneRemainingStock(int remainingStock) {
            Stock s = buildStock(remainingStock);

            boolean hasRemainingQuantity = s.hasRemainingQuantity();
            assertThat(hasRemainingQuantity).isTrue();
        }

    }

    @Nested
    @DisplayName("Reduce tests")
    class Reduce {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -3, -6154, -6131, -6423, -97894, -741})
        @DisplayName("Test with negative reduce quantity throws IllegalArgumentException")
        void testWithNegativeReduceQuantity(int negativeReduceQuantity) {
            Stock s = buildStock(1);

            assertThrows(IllegalArgumentException.class, () -> s.reduce(negativeReduceQuantity));
        }

        @Test
        @DisplayName("Test with zero stock throws IllegalArgumentException")
        void testWithZeroStock() {
            Stock s = buildStock(0);

            assertThrows(IllegalArgumentException.class, () -> s.reduce(1));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 6, 54, 75, 661, 220, 547, 501, 138, 802})
        @DisplayName("Test with too much reduce quantity throws IllegalArgumentException")
        void testWithTooMuchReduceQuantity(int stock) {
            Stock s = buildStock(stock);

            assertThrows(IllegalArgumentException.class, () -> s.reduce(stock + 1));
        }

        @Test
        @DisplayName("Test with one stock and one reduce quantity do noting")
        void testWithOneOne() {
            Stock s = buildStock(1);

            assertDoesNotThrow(() -> s.reduce(1));
        }

        @ParameterizedTest
        @ValueSource(ints = {2, 5, 665, 4, 96})
        @DisplayName("Test with superior or equal stock than reduce quantity do nothing")
        void testWithAllCorrect(int stock) {
            Stock s = buildStock(stock);

            assertDoesNotThrow(() -> s.reduce(stock - 1));

            if (stock > 2) {
                s.setRemainingQuantity(stock);
                assertDoesNotThrow(() -> s.reduce(stock - 2));
            }
        }
    }

    private Stock buildStock(int remainingQuantity) {
        return Stock.builder().remainingQuantity(remainingQuantity).build();
    }

}
