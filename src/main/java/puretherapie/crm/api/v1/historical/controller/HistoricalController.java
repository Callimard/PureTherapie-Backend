package puretherapie.crm.api.v1.historical.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.historical.controller.dto.HistoricalViewDTO;
import puretherapie.crm.api.v1.historical.service.HistoricalService;
import puretherapie.crm.data.historical.HistoricalView;
import puretherapie.crm.data.historical.repository.HistoricalViewRepository;
import puretherapie.crm.data.person.user.User;

import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.historical.controller.HistoricalController.HISTORICAL_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(HISTORICAL_URL)
public class HistoricalController {

    // Constants.

    public static final String HISTORICAL_URL = API_V1_URL + "/historical";

    public static final String SET_HISTORICAL_VIEWED = "/setViewed";

    // Variables.

    private HistoricalService historicalService;
    private HistoricalViewRepository historicalViewRepository;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @GetMapping
    public List<HistoricalViewDTO> getHistoricalViews(@RequestParam(name = "filter", required = false, defaultValue = "0") int filter,
                                                      Authentication authentication) {
        if (authentication.getPrincipal() instanceof User user) {
            List<HistoricalView> historicalViews;
            if (filter == 0) {
                historicalViews = historicalViewRepository.findByUser(user);
            } else if (filter == 1) {
                historicalViews = historicalViewRepository.findByUserAndViewed(user, true);
            } else {
                historicalViews = historicalViewRepository.findByUserAndViewed(user, false);
            }
            return historicalViews.stream().map(HistoricalView::transform).toList();
        } else {
            throw new IllegalArgumentException("The principal is not a User instance");
        }
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS', 'ROLE_MAMY', 'ROLE_SECRETARY')")
    @PutMapping("/{idHistoricalView}" + SET_HISTORICAL_VIEWED)
    public void setHistoricalViewed(@PathVariable(name = "idHistoricalView") int idHistoricalView) {
        historicalService.setHistoricalViewed(idHistoricalView);
    }

}
