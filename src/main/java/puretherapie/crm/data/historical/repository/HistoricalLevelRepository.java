package puretherapie.crm.data.historical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.historical.HistoricalLevel;

import static puretherapie.crm.data.historical.HistoricalLevel.ALL_ROLES_LEVEL;

@Repository
public interface HistoricalLevelRepository extends JpaRepository<HistoricalLevel, Integer> {

    HistoricalLevel findByIdHistoricalLevel(Integer idNotificationLevel);

    HistoricalLevel findByHistoricalLevelName(String levelName);

    default HistoricalLevel getAllRolesLevel() {
        return findByHistoricalLevelName(ALL_ROLES_LEVEL);
    }
}