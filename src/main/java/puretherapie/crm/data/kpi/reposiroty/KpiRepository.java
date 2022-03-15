package puretherapie.crm.data.kpi.reposiroty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.kpi.Kpi;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, Integer> {
}