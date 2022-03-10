package puretherapie.crm.api.v1.historical.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.historical.HistoricalView;
import puretherapie.crm.data.historical.repository.HistoricalViewRepository;

@Slf4j
@AllArgsConstructor
@Service
public class HistoricalService {

    // Constants.

    public static final String HISTORICAL_VIEW_NOT_FOUND_ERROR = "historical_view_not_found_error";

    // Variables.

    private final HistoricalViewRepository historicalViewRepository;

    // Methods.

    public void setHistoricalViewed(int idHistoricalView) {
        HistoricalView historicalView = verifyHistoricalView(idHistoricalView);
        historicalView.setViewed(true);
        historicalViewRepository.save(historicalView);
    }

    private HistoricalView verifyHistoricalView(int idHistoricalView) {
        HistoricalView historicalView = historicalViewRepository.findByIdHistoricalView(idHistoricalView);
        if (historicalView == null)
            throw new HistoricalServiceException(HISTORICAL_VIEW_NOT_FOUND_ERROR);

        return historicalView;
    }

    // Exception.

    public static class HistoricalServiceException extends RuntimeException {
        public HistoricalServiceException(String message) {
            super(message);
        }
    }

}
