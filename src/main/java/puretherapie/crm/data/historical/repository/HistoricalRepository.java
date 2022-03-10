package puretherapie.crm.data.historical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.historical.Historical;

import java.util.List;

@Repository
public interface HistoricalRepository extends JpaRepository<Historical, Integer> {

    List<Historical> findByHistoricalTitle(String historicalTitle);

    void deleteByHistoricalTitle(String historicalTitle);

}