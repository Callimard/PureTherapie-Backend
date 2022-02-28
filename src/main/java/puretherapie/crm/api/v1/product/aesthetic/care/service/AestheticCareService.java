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
        verifyName(acName);
        verifyPrice(price);
        verifyExecutionTime(executionTime);
        saveAC(acName, price, executionTime);
    }

    public void updateAestheticCare(int idAestheticCare, String acName, double price, int executionTime) {
        verifyName(acName);
        verifyPrice(price);
        verifyExecutionTime(executionTime);
        updateAC(idAestheticCare, acName, price, executionTime);
    }

    private void verifyName(String acName) {
        if (acName == null || acName.isBlank())
            throw new AestheticCareException("AC name cannot be null or blank");
    }

    private void verifyPrice(double price) {
        if (price < 0.0d)
            throw new AestheticCareException("AC price must be greater or equal to 0.0");
    }

    private void verifyExecutionTime(int executionTime) {
        if (executionTime < 1)
            throw new AestheticCareException("AC execution time must be greater or equal to 1");
    }

    private void saveAC(String acName, double price, int executionTime) {
        AestheticCare aestheticCare = aestheticCareRepository.save(buildAC(acName, price, executionTime));
        log.info("Dave new AC => {}", aestheticCare);
    }

    private AestheticCare buildAC(String acName, double price, int executionTime) {
        return AestheticCare.builder()
                .name(acName)
                .price(price)
                .timeExecution(executionTime)
                .build();
    }

    private void updateAC(int idAestheticCare, String acName, double price, int executionTime) {
        AestheticCare aestheticCare = aestheticCareRepository.findByIdAestheticCare(idAestheticCare);
        aestheticCare.setName(acName);
        aestheticCare.setPrice(price);
        aestheticCare.setTimeExecution(executionTime);
        aestheticCare = aestheticCareRepository.save(aestheticCare);
        log.info("Update aesthetic care => {}", aestheticCare);
    }

    // Exception.

    public static class AestheticCareException extends RuntimeException {
        public AestheticCareException(String message) {
            super(message);
        }
    }

}
