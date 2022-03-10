package puretherapie.crm.data.historical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puretherapie.crm.data.historical.HistoricalView;
import puretherapie.crm.data.person.user.User;

import java.util.List;

@Repository
public interface HistoricalViewRepository extends JpaRepository<HistoricalView, Integer> {

    HistoricalView findByIdHistoricalView(int idHistoricalView);

    List<HistoricalView> findByUser(User user);

    List<HistoricalView> findByUserAndViewed(User user, boolean viewed);
}