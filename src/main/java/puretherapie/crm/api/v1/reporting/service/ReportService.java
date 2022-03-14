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
import puretherapie.crm.tool.service.PDFGeneratorService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class ReportService {

    // Variables.

    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final PDFGeneratorService pdfGeneratorService;
    private final KPIFactory kpiFactory;

    // Methods.

    public Report saveDayReport(LocalDate day) {
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
        return reportType.reportTypePath() + "/" + System.currentTimeMillis() + ".pdf";
    }

    public void executeReport(Report report) {
        try {
            ReportType.createReportTypeDirectoryIfNotExists(report.getReportType());
            generateReportFile(report);
        } catch (IOException e) {
            throw new ReportException("Fail to create report directory", e);
        }
    }

    private void generateReportFile(Report report) {
        Map<String, Object> args = new HashMap<>();
        args.put("reportTitle", report.generateReportTile());
        args.put("reportResults", report.execute(kpiFactory));
        pdfGeneratorService.generatePdfFile("report-template", args, report.getFile());
    }

    // Exceptions.

    public static class ReportException extends RuntimeException {
        public ReportException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
