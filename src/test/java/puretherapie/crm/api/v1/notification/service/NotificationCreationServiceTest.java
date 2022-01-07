package puretherapie.crm.api.v1.notification.service;

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
import puretherapie.crm.data.notification.NotificationLevel;
import puretherapie.crm.data.notification.repository.NotificationLevelRepository;
import puretherapie.crm.data.notification.repository.NotificationRepository;
import puretherapie.crm.data.notification.repository.NotificationViewRepository;
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
import static puretherapie.crm.data.notification.NotificationLevel.ALL_ROLES_LEVEL;

@Slf4j
@SpringBootTest
@DisplayName("Notification Service Tests")
public class NotificationCreationServiceTest {

    private static final String CORRECT_TITLE = "CORRECT_TITLE";
    private static final String CORRECT_TEXT = "CORRECT_TEXT";

    @Autowired
    private NotificationCreationService notificationCreationService;

    @Nested
    @DisplayName("createNotification tests")
    class CreateNotification {

        @Nested
        @DisplayName("With NotificationLevel name arg")
        class WithNotificationLevelNameAgr {

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "      "})
            @DisplayName("Test if createNotification returns false if level name is blank")
            void testWithBlankLevelName(String blank) {
                boolean success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, blank, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns false with unknown level name")
            void testWithUnknownLevelName() {
                prepareMinimalContext();
                prepareUnknownNotificationLevel();
                boolean success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, UNKNOWN_NOTIFICATION_LEVEL, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns false with known level name but without associated roles")
            void testWithKnownLevelNameWithoutRoles() {
                prepareMinimalContext();
                prepareFactitiousNotificationLevel();
                boolean success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, FACTITIOUS_NOTIFICATION_LEVEL, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns true with known level name and with associated roles")
            void testWithKnownLevelNameWithRole() {
                prepareMinimalContext();
                prepareAllRolesLevel();
                boolean success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, ALL_ROLES_LEVEL, false);

                verifySuccess(success);
            }

        }

        @Nested
        @DisplayName("With NotificationLevel object arg")
        class WithNotificationLevelArg {

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "      "})
            @DisplayName("Test if createNotification returns false if notification level name is blank")
            void testWithBlankLevelName(String blank) {
                NotificationLevel level = NotificationLevel.builder().notificationLevelName(blank).build();
                boolean success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, level, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns false and rollback work if notification view creation fail")
            void testWithFailDuringNotificationViewCreation() {
                prepareMinimalContext();
                prepareDefaultAllRolesLevel();
                given(mockNotificationViewRepository.save(any())).willThrow(new IllegalArgumentException());

                boolean success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, (NotificationLevel) null, false);

                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns false if no roles is found for a correct notification level")
            void testWithNonRoleNotificationLevel() {
                prepareMinimalContext();
                prepareFactitiousNotificationLevel();

                NotificationLevel level = mockNotificationLevelRepository.findByNotificationLevelName(FACTITIOUS_NOTIFICATION_LEVEL);
                log.debug("False level find = {}", level);
                boolean success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, level, false);

                verifyFail(success);
            }

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "   "})
            @DisplayName("Test if createNotification returns false if black title or text is pass in parameter")
            void testWithBlankTitleOrText(String blank) {
                prepareMinimalContext();
                prepareDefaultAllRolesLevel();

                boolean success = notificationCreationService.createNotification(blank, CORRECT_TEXT, (NotificationLevel) null, false);
                verifyFail(success);

                success = notificationCreationService.createNotification(CORRECT_TITLE, blank, (NotificationLevel) null, false);
                verifyFail(success);

                success = notificationCreationService.createNotification(blank, blank, (NotificationLevel) null, false);
                verifyFail(success);

                success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT,
                                                                         NotificationLevel.builder().notificationLevelName(blank).build(),
                                                                         false);
                verifyFail(success);
            }

            @Test
            @DisplayName("Test if createNotification returns true and create notification for all users with null level")
            void testWithNullLevel() {
                prepareMinimalContext();
                prepareDefaultAllRolesLevel();
                prepareAllRolesLevel();

                boolean success = notificationCreationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, (NotificationLevel) null, false);

                verifySuccess(success);

                verify(mockNotificationViewRepository, times(2)).save(any());
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
    private NotificationRepository mockNotificationRepository;

    @MockBean
    private NotificationLevelRepository mockNotificationLevelRepository;
    @Mock
    private NotificationLevel mockNotificationLevel;
    private static final String UNKNOWN_NOTIFICATION_LEVEL = "UNKNOWN_LEVEL";
    private static final String FACTITIOUS_NOTIFICATION_LEVEL = "FACTITIOUS_NOTIFICATION_LEVEL";

    @MockBean
    private UserRepository mockUserRepository;

    @MockBean
    private NotificationViewRepository mockNotificationViewRepository;

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
        given(mockNotificationRepository.save(any())).willReturn(null);
    }

    private void prepareRoleRepository() {
        List<Role> roles = new ArrayList<>();
        roles.add(mockRole);
        given(mockRoleRepository.findByNotificationLevels(any())).willReturn(roles);
    }

    private void prepareUnknownNotificationLevel() {
        given(mockNotificationLevelRepository.findByNotificationLevelName(UNKNOWN_NOTIFICATION_LEVEL)).willReturn(null);
    }

    private void prepareFactitiousNotificationLevel() {
        given(mockNotificationLevelRepository.findByNotificationLevelName(FACTITIOUS_NOTIFICATION_LEVEL)).willReturn(mockNotificationLevel);
    }

    private void prepareDefaultAllRolesLevel() {
        given(mockNotificationLevelRepository.getAllRolesLevel()).willReturn(mockNotificationLevel);
    }

    private void prepareAllRolesLevel() {
        given(mockNotificationLevelRepository.findByNotificationLevelName(ALL_ROLES_LEVEL)).willReturn(mockNotificationLevel);
        given(mockNotificationLevel.getNotificationLevelName()).willReturn(ALL_ROLES_LEVEL);
    }

}
