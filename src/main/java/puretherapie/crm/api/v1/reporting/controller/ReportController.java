package puretherapie.crm.api.v1.reporting.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import puretherapie.crm.api.v1.reporting.service.ReportService;
import puretherapie.crm.data.reporting.Report;

import java.time.LocalDate;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.reporting.controller.ReportController.REPORTS_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(REPORTS_URL)
public class ReportController {

    // Constants.

    public static final String REPORTS_URL = API_V1_URL + "/reports";

    // Variables.

    private final ReportService reportService;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @GetMapping
    public void generatePDF() {
        Report report = reportService.saveAnnualReport(LocalDate.parse("1996-12-15"));
        reportService.executeReport(report);
    }

}
