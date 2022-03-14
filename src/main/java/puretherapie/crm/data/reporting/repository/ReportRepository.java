package puretherapie.crm.data.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.reporting.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    Report findByIdReport(int idReport);

}