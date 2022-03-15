package puretherapie.crm.data.reporting.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.reporting.Report;
import puretherapie.crm.data.reporting.ReportType;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    Report findByIdReport(int idReport);

    List<Report> findByReportTypeOrderByDateBeginDescDateEndDesc(ReportType reportType, Pageable pageable);

}