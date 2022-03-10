package puretherapie.crm.api.v1.historical.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import puretherapie.crm.data.historical.Historical;
import puretherapie.crm.data.historical.HistoricalLevel;
import puretherapie.crm.data.historical.HistoricalView;
import puretherapie.crm.data.historical.repository.HistoricalLevelRepository;
import puretherapie.crm.data.historical.repository.HistoricalRepository;
import puretherapie.crm.data.historical.repository.HistoricalViewRepository;
import puretherapie.crm.data.person.user.Role;
import puretherapie.crm.data.person.user.User;
import puretherapie.crm.data.person.user.repository.RoleRepository;
import puretherapie.crm.data.person.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class HistoricalCreationService {

    // Variables.

    private final HistoricalRepository historicalRepository;
    private final HistoricalLevelRepository historicalLevelRepository;
    private final HistoricalViewRepository historicalViewRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Methods.

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createHistorical(String title, String text, String levelName, boolean isAnAlert) {
        if (unCorrectArgs(title, text, levelName)) return false;

        HistoricalLevel level = historicalLevelRepository.findByHistoricalLevelName(levelName);
        if (level == null) {
            log.error("Level not found for the level name {}", levelName);
            return false;
        }

        return createHistorical(title, text, level, isAnAlert);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createHistorical(String title, String text, HistoricalLevel historicalLevel, boolean isAnAlert) {
        if (historicalLevel == null)
            historicalLevel = historicalLevelRepository.getAllRolesLevel();

        if (unCorrectArgs(title, text, historicalLevel.getHistoricalLevelName())) return false;

        try {
            Historical historical = buildAndSaveHistorical(title, text, historicalLevel, isAnAlert);

            List<Role> roles = findRoles(historicalLevel);
            if (roles != null && !roles.isEmpty()) {
                createHistoricalView(historical, searchUserFromRole(roles));
                return true;
            } else {
                throw new NoRolesFoundException("No roles find for this notification level %s".formatted(historicalLevel));
            }
        } catch (Exception e) {
            log.error("Fail notification creation. ROLLBACK DONE. Error msg = {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private List<Role> findRoles(HistoricalLevel historicalLevel) {
        List<Role> roles = roleRepository.findByHistoricalLevels(historicalLevel);
        log.debug("For level {} find roles {}", historicalLevel, roles);
        return roles;
    }

    private Historical buildAndSaveHistorical(String notificationTitle, String text, HistoricalLevel historicalLevel, boolean isAnAlert) {
        Historical historical = historicalRepository.save(buildHistorical(notificationTitle, text, historicalLevel, isAnAlert));
        log.info("Create Notification {}", historical);
        return historical;
    }

    private boolean unCorrectArgs(String notificationTitle, String text, String levelName) {
        if ((notificationTitle == null || notificationTitle.isBlank()) || (text == null || text.isBlank()) ||
                (levelName == null || levelName.isBlank())) {
            log.error("Notification title, text or level name must not be null or blank");
            return true;
        }
        return false;
    }

    private Historical buildHistorical(String historicalTitle, String text, HistoricalLevel historicalLevel, boolean isAnAlert) {
        return Historical.builder()
                .historicalTitle(historicalTitle)
                .text(text)
                .type(isAnAlert)
                .historicalLevel(historicalLevel)
                .creationDate(LocalDateTime.now())
                .build();
    }

    private Iterable<User> searchUserFromRole(List<Role> roles) {
        Set<User> users = new HashSet<>();
        roles.forEach(r -> users.addAll(userRepository.findByRoles(r)));
        return users;
    }

    private HistoricalView buildHistoricalView(Historical historical, User user) {
        return HistoricalView.builder()
                .viewed(false)
                .historical(historical)
                .user(user)
                .build();
    }

    private void createHistoricalView(Historical historical, Iterable<User> users) {
        for (User user : users) {
            HistoricalView historicalView = historicalViewRepository.save(buildHistoricalView(historical, user));
            log.info("Save NotificationView {}", historicalView);
        }
    }

    private static class NoRolesFoundException extends RuntimeException {
        public NoRolesFoundException(String msg) {
            super(msg);
        }
    }

}
