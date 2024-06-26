package puretherapie.crm.api.v1.reporting.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import puretherapie.crm.api.v1.kpi.service.KPIFactory;
import puretherapie.crm.data.kpi.Kpi;
import puretherapie.crm.data.reporting.Report;
import puretherapie.crm.data.reporting.ReportType;
import puretherapie.crm.data.reporting.repository.ReportRepository;
import puretherapie.crm.data.reporting.repository.ReportTypeRepository;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class ReportService {

    // Variables.

    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final PDFReportGeneratorService pdfReportGeneratorService;
    private final KPIFactory kpiFactory;

    // Methods.

    public void generateCustomReport(LocalDate begin, LocalDate end) {
        ReportType reportType = reportTypeRepository.findByName("custom");
        Report report = saveReport(reportType, begin, end, new ArrayList<>(reportType.getConfigurationKpis()));
        executeReport(report);
    }

    public void generateAnnualReport(LocalDate oneDayOfYear) {
        Report report = saveAnnualReport(oneDayOfYear);
        executeReport(report);
    }

    public void generateMonthlyReport(LocalDate oneDayOfMonth) {
        Report report = saveMonthlyReport(oneDayOfMonth);
        executeReport(report);
    }

    public void generateWeeklyReport(LocalDate oneDayOfWeek) {
        Report report = saveWeeklyReport(oneDayOfWeek);
        executeReport(report);
    }

    public void generateDailyReport(LocalDate day) {
        Report report = saveDailyReport(day);
        executeReport(report);
    }

    public Report saveAnnualReport(LocalDate day) {
        LocalDate firstDayYear = day.withDayOfYear(1);
        LocalDate lastDayYear = day.withDayOfYear(day.lengthOfYear());
        ReportType reportType = reportTypeRepository.findByName(ReportType.BasicReportType.YEAR.reportTypeName());
        log.debug("First day year = {}", firstDayYear);
        log.debug("Last day year = {}", lastDayYear);
        return saveReport(reportType, firstDayYear, lastDayYear, new ArrayList<>(reportType.getConfigurationKpis()));
    }

    public Report saveMonthlyReport(LocalDate day) {
        LocalDate firstDayMonth = day.withDayOfMonth(1);
        LocalDate lastDayMonth = day.withDayOfMonth(day.lengthOfMonth());
        ReportType reportType = reportTypeRepository.findByName(ReportType.BasicReportType.MONTH.reportTypeName());
        return saveReport(reportType, firstDayMonth, lastDayMonth, new ArrayList<>(reportType.getConfigurationKpis()));
    }

    public Report saveWeeklyReport(LocalDate day) {
        LocalDate firstDayWeek = day.minusDays(day.getDayOfWeek().getValue() - 1L);
        LocalDate lastDayWeek = day.plusDays(7L - day.getDayOfWeek().getValue());
        ReportType reportType = reportTypeRepository.findByName(ReportType.BasicReportType.WEEK.reportTypeName());
        return saveReport(reportType, firstDayWeek, lastDayWeek, new ArrayList<>(reportType.getConfigurationKpis()));
    }

    public Report saveDailyReport(LocalDate day) {
        ReportType reportType = reportTypeRepository.findByName(ReportType.BasicReportType.DAY.reportTypeName());
        return saveReport(reportType, day, day, new ArrayList<>(reportType.getConfigurationKpis()));
    }

    public Report saveReport(ReportType reportType, LocalDate begin, LocalDate end, List<Kpi> config) {
        Report report = reportRepository.save(buildReport(reportType, begin, end, config));
        log.info("Save report => {}", report);
        return report;
    }

    private Report buildReport(ReportType reportType, LocalDate begin, LocalDate end, List<Kpi> config) {
        return Report.builder()
                .dateBegin(begin)
                .dateEnd(end)
                .file(generateReportPath(reportType))
                .reportType(reportType)
                .configurationKpis(new HashSet<>(config))
                .build();
    }

    private String generateReportPath(ReportType reportType) {
        return "/" + reportType.getName() + "/" + System.currentTimeMillis() + ".pdf";
    }

    public void executeReport(Report report) {
        generateReportFile(report);
    }

    private void generateReportFile(Report report) {
        Map<String, Object> args = new HashMap<>();
        args.put("reportTitle", report.generateReportTile());
        args.put("reportResults", report.execute(kpiFactory));
        pdfReportGeneratorService.generatePdfFile("report-template", args, report.getFile());
    }

}
