package puretherapie.crm.api.v1.reporting.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import puretherapie.crm.api.v1.reporting.controller.dto.ReportPackageDTO;
import puretherapie.crm.api.v1.reporting.service.ReportService;
import puretherapie.crm.data.reporting.Report;
import puretherapie.crm.data.reporting.ReportType;
import puretherapie.crm.data.reporting.repository.ReportRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static puretherapie.crm.api.v1.ApiV1.API_V1_URL;
import static puretherapie.crm.api.v1.reporting.controller.ReportController.REPORTS_URL;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(REPORTS_URL)
public class ReportController {

    // Constants.

    public static final String REPORTS_URL = API_V1_URL + "/reports";

    public static final String CUSTOM_REPORTS = "/custom";
    public static final String DAILY_REPORTS = "/daily";
    public static final String WEEKLY_REPORTS = "/weekly";
    public static final String MONTHLY_REPORTS = "/monthly";
    public static final String ANNUAL_REPORTS = "/annual";

    // Variables.

    private final ReportRepository reportRepository;
    private final ReportService reportService;

    // Methods.

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @GetMapping
    public ReportPackageDTO getAllReports() {
        ReportPackageDTO reportPackageDTO = prepareReportPackage();
        fillReportPackage(reportRepository.findAll(), reportPackageDTO);
        return reportPackageDTO;
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping("/{idReport}")
    public void executeReport(@PathVariable(name = "idReport") int idReport) {
        Report report = reportRepository.findByIdReport(idReport);
        reportService.executeReport(report);
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping(CUSTOM_REPORTS)
    public void generateCustomReport(@RequestParam(name = "begin") String begin, @RequestParam(name = "end") String end) {
        reportService.generateCustomReport(LocalDate.parse(begin), LocalDate.parse(end));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping(DAILY_REPORTS)
    public void generateDailyReport(@RequestParam(name = "day") String day) {
        reportService.generateDailyReport(LocalDate.parse(day));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping(WEEKLY_REPORTS)
    public void generateWeeklyReport(@RequestParam(name = "oneDayOfWeek") String oneDayOfWeek) {
        reportService.generateWeeklyReport(LocalDate.parse(oneDayOfWeek));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping(MONTHLY_REPORTS)
    public void generateMonthlyReport(@RequestParam(name = "oneDayOfMonth") String oneDayOfMonth) {
        reportService.generateMonthlyReport(LocalDate.parse(oneDayOfMonth));
    }

    @PreAuthorize("isAuthenticated() && hasAnyRole('ROLE_BOSS')")
    @PostMapping(ANNUAL_REPORTS)
    public void generateAnnualReport(@RequestParam(name = "oneDayOfYear") String oneDayOfYear) {
        reportService.generateAnnualReport(LocalDate.parse(oneDayOfYear));
    }

    private void fillReportPackage(List<Report> reports, ReportPackageDTO reportPackageDTO) {
        for (Report report : reports) {
            if (report.getReportType().typeToEnum() == ReportType.BasicReportType.DAY) {
                reportPackageDTO.getDailyReports().add(report.transform());
            } else if (report.getReportType().typeToEnum() == ReportType.BasicReportType.WEEK) {
                reportPackageDTO.getWeeklyReports().add(report.transform());
            } else if (report.getReportType().typeToEnum() == ReportType.BasicReportType.MONTH) {
                reportPackageDTO.getMonthlyReports().add(report.transform());
            } else if (report.getReportType().typeToEnum() == ReportType.BasicReportType.YEAR) {
                reportPackageDTO.getAnnualReports().add(report.transform());
            } else {
                reportPackageDTO.getOtherReports().add(report.transform());
            }
        }
    }

    private ReportPackageDTO prepareReportPackage() {
        return ReportPackageDTO.builder()
                .dailyReports(new ArrayList<>())
                .weeklyReports(new ArrayList<>())
                .monthlyReports(new ArrayList<>())
                .annualReports(new ArrayList<>())
                .otherReports(new ArrayList<>())
                .build();
    }

}
