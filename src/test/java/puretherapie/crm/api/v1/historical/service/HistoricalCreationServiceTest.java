package puretherapie.crm.api.v1.historical.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import puretherapie.crm.data.historical.HistoricalLevel;
import puretherapie.crm.data.historical.repository.HistoricalLevelRepository;
import puretherapie.crm.data.historical.repository.HistoricalRepository;
import puretherapie.crm.data.historical.repository.HistoricalViewRepository;
import puretherapie.crm.data.person.user.Role;
import puretherapie.crm.data.person.user.User;
import puretherapie.crm.data.person.user.repository.RoleRepository;
import puretherapie.crm.data.person.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static puretherapie.crm.data.historical.HistoricalLevel.ALL_ROLES_LEVEL;

@Slf4j
@SpringBootTest
@DisplayName("Notification Service Tests")
public class HistoricalCreationServiceTest {

    private static final String CORRECT_TITLE = "CORRECT_TITLE";
    private static final String CORRECT_TEXT = "CORRECT_TEXT";

    @Autowired
    private HistoricalCreationService historicalCreationService;

    @Nested
    @DisplayName("createNotification tests")
    class CreateHistorical {

        @Nested
        @DisplayName("With NotificationLevel name arg")
        class WithHistoricalLevelNameAgr {

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "      "})
            @DisplayName("Test if createNotification returns false if level name is blank")
            void testWithBlankLevelName(String blank) {
                boolean success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT, blank, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns false with unknown level name")
            void testWithUnknownLevelName() {
                prepareMinimalContext();
                prepareUnknownNotificationLevel();
                boolean success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT, UNKNOWN_NOTIFICATION_LEVEL, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns false with known level name but without associated roles")
            void testWithKnownLevelNameWithoutRoles() {
                prepareMinimalContext();
                prepareFactitiousNotificationLevel();
                boolean success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT, FACTITIOUS_NOTIFICATION_LEVEL, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns true with known level name and with associated roles")
            void testWithKnownLevelNameWithRole() {
                prepareMinimalContext();
                prepareAllRolesLevel();
                boolean success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT, ALL_ROLES_LEVEL, false);

                verifySuccess(success);
            }

        }

        @Nested
        @DisplayName("With NotificationLevel object arg")
        class WithHistoricalLevelArg {

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "      "})
            @DisplayName("Test if createNotification returns false if notification level name is blank")
            void testWithBlankLevelName(String blank) {
                HistoricalLevel level = HistoricalLevel.builder().historicalLevelName(blank).build();
                boolean success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT, level, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns false and rollback work if notification view creation fail")
            void testWithFailDuringNotificationViewCreation() {
                prepareMinimalContext();
                prepareDefaultAllRolesLevel();
                given(mockHistoricalViewRepository.save(any())).willThrow(new IllegalArgumentException());

                boolean success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT, (HistoricalLevel) null, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns false if no roles is found for a correct notification level")
            void testWithNonRoleNotificationLevel() {
                prepareMinimalContext();
                prepareFactitiousNotificationLevel();

                HistoricalLevel level = mockHistoricalLevelRepository.findByHistoricalLevelName(FACTITIOUS_NOTIFICATION_LEVEL);
                log.debug("False level find = {}", level);
                boolean success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT, level, false);

                verifyFail(success);
            }

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "   "})
            @DisplayName("Test if createNotification returns false if black title or text is pass in parameter")
            void testWithBlankTitleOrText(String blank) {
                prepareMinimalContext();
                prepareDefaultAllRolesLevel();

                boolean success = historicalCreationService.createHistorical(blank, CORRECT_TEXT, (HistoricalLevel) null, false);
                verifyFail(success);

                success = historicalCreationService.createHistorical(CORRECT_TITLE, blank, (HistoricalLevel) null, false);
                verifyFail(success);

                success = historicalCreationService.createHistorical(blank, blank, (HistoricalLevel) null, false);
                verifyFail(success);

                success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT,
                                                                     HistoricalLevel.builder().historicalLevelName(blank).build(),
                                                                     false);
                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns true and create notification for all users with null level")
            void testWithNullLevel() {
                prepareMinimalContext();
                prepareDefaultAllRolesLevel();
                prepareAllRolesLevel();

                boolean success = historicalCreationService.createHistorical(CORRECT_TITLE, CORRECT_TEXT, (HistoricalLevel) null, false);

                verifySuccess(success);

                verify(mockHistoricalViewRepository, times(2)).save(any());
            }
        }
    }

    private void verifySuccess(boolean success) {
        assertThat(success).isTrue();
    }

    private void verifyFail(boolean success) {
        assertThat(success).isFalse();
    }

    // Context.

    @MockBean
    private HistoricalRepository mockHistoricalRepository;

    @MockBean
    private HistoricalLevelRepository mockHistoricalLevelRepository;
    @Mock
    private HistoricalLevel mockHistoricalLevel;
    private static final String UNKNOWN_NOTIFICATION_LEVEL = "UNKNOWN_LEVEL";
    private static final String FACTITIOUS_NOTIFICATION_LEVEL = "FACTITIOUS_NOTIFICATION_LEVEL";

    @MockBean
    private UserRepository mockUserRepository;

    @MockBean
    private HistoricalViewRepository mockHistoricalViewRepository;

    @MockBean
    private RoleRepository mockRoleRepository;
    @Mock
    private Role mockRole;

    @Mock
    private User mockBoss;

    @Mock
    private User mockSecretary;

    private void prepareMinimalContext() {
        prepareNotificationRepository();
        prepareUserRepository();
        prepareRoleRepository();
    }

    private void prepareUserRepository() {
        List<User> users = new ArrayList<>();
        users.add(mockBoss);
        users.add(mockSecretary);

        given(mockUserRepository.findByRoles(any())).willReturn(users);
    }

    private void prepareNotificationRepository() {
        given(mockHistoricalRepository.save(any())).willReturn(null);
    }

    private void prepareRoleRepository() {
        List<Role> roles = new ArrayList<>();
        roles.add(mockRole);
        given(mockRoleRepository.findByHistoricalLevels(any())).willReturn(roles);
    }

    private void prepareUnknownNotificationLevel() {
        given(mockHistoricalLevelRepository.findByHistoricalLevelName(UNKNOWN_NOTIFICATION_LEVEL)).willReturn(null);
    }

    private void prepareFactitiousNotificationLevel() {
        given(mockHistoricalLevelRepository.findByHistoricalLevelName(FACTITIOUS_NOTIFICATION_LEVEL)).willReturn(mockHistoricalLevel);
    }

    private void prepareDefaultAllRolesLevel() {
        given(mockHistoricalLevelRepository.getAllRolesLevel()).willReturn(mockHistoricalLevel);
    }

    private void prepareAllRolesLevel() {
        given(mockHistoricalLevelRepository.findByHistoricalLevelName(ALL_ROLES_LEVEL)).willReturn(mockHistoricalLevel);
        given(mockHistoricalLevel.getHistoricalLevelName()).willReturn(ALL_ROLES_LEVEL);
    }

}
