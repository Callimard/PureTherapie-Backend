package puretherapie.crm.data.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.reporting.ReportType;

@Repository
public interface ReportTypeRepository extends JpaRepository<ReportType, Integer> {

    ReportType findByName(String name);

}