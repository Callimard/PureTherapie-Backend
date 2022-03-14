package puretherapie.crm.api.v1.reporting.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import puretherapie.crm.data.reporting.Report;

import java.time.LocalDate;

@Slf4j
@AllArgsConstructor
@Service
public class AutomaticReportingService {

    // Variables.

    private final ReportService reportService;

    // Methods.

    @Async
    @Scheduled(cron = "0 0 22 * * *", zone = "Europe/Paris")
    public void generateDayReport() {
        log.info("Generate daily report");
        reportService.generateDailyReport();
    }

    @Async
    @Scheduled(cron = "0 0 22 * * FRI", zone = "Europe/Paris")
    public void generateWeeklyReport() {
        log.info("Generate weekly report");
        reportService.generateWeeklyReport();
    }

    @Async
    @Scheduled(cron = "0 0 0 1 * *", zone = "Europe/Paris")
    public void generateMonthReport() {
        log.info("Generate monthly report");
        Report report = reportService.saveMonthlyReport(LocalDate.now().minusDays(1));
        reportService.executeReport(report);
    }

    @Async
    @Scheduled(cron = "0 0 23 31 12 ?", zone = "Europe/Paris")
    public void generateYearReport() {
        log.info("Generate annual report");
        reportService.generateAnnualReport();
    }

}
