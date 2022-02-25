package puretherapie.crm.api.v1.product.aesthetic.care.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.product.aesthetic.care.AestheticCare;
import puretherapie.crm.data.product.aesthetic.care.repository.AestheticCareRepository;

@Slf4j
@AllArgsConstructor
@Service
public class AestheticCareService {

    // Variables.

    private final AestheticCareRepository aestheticCareRepository;

    // Methods.

    /**
     * Create the aesthetic care
     *
     * @param acName        the aesthetic care name
     * @param price         the aesthetic care price
     * @param executionTime the aesthetic care execution time
     */
    public void createAestheticCare(String acName, double price, int executionTime) {
        AestheticCare aestheticCare = AestheticCare.builder()
                .name(acName)
                .price(price)
                .timeExecution(executionTime)
                .build();
        aestheticCare = aestheticCareRepository.save(aestheticCare);
        log.info("Dave new AC => {}", aestheticCare);
    }

}
