package puretherapie.crm.api.v1.kpi.conrtoller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.kpi.conrtoller.dto.KpiDTO;
import puretherapie.crm.api.v1.kpi.service.ExecutableKPI;
import puretherapie.crm.api.v1.kpi.service.KPIFactory;
import puretherapie.crm.data.kpi.Kpi;
import puretherapie.crm.data.kpi.reposiroty.KpiRepository;

import java.time.LocalDate;
import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.kpi.conrtoller.KPIController.KPIS_URL;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(KPIS_URL)
public class KPIController {

    // Constants.

    public static final String KPIS = "/kpis";
    public static final String KPIS_URL = API_V1_URL + KPIS;

    public static final String SPECIFIED_KPI = "/{kpiName}";
    public static final String SPECIFIED_KPI_URL = KPIS_URL + SPECIFIED_KPI;

    public static final String KPI_FILTER = "/filter";

    // Variables.

    private final KpiRepository kpiRepository;
    private final KPIFactory kpiFactory;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @GetMapping
    public List<KpiDTO> getAllKpis() {
        return kpiRepository.findAll().stream().map(Kpi::transform).toList();
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @GetMapping(SPECIFIED_KPI)
    public ExecutableKPI.KPIRes executeKpi(@PathVariable(name = "kpiName") String kpiName,
                                           @RequestParam(name = "begin") String begin,
                                           @RequestParam(name = "end") String end) {
        return kpiFactory.getExecutableKPI(kpiName).execute(LocalDate.parse(begin), LocalDate.parse(end));
    }
}
