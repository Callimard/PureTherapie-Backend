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
import org.springframework.test.context.jdbc.Sql;
import puretherapie.crm.data.notification.NotificationLevel;
import puretherapie.crm.data.notification.repository.NotificationLevelRepository;
import puretherapie.crm.data.notification.repository.NotificationRepository;
import puretherapie.crm.data.notification.repository.NotificationViewRepository;
import puretherapie.crm.data.person.user.User;
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
public class NotificationServiceTest {

    private static final String UNKNOWN_NOTIFICATION_LEVEL = "UNKNOWN_LEVEL";

    private static final String FALSE_NOTIFICATION_LEVEL = "FALSE_NOTIFICATION_LEVEL";

    private static final String CORRECT_TITLE = "CORRECT_TITLE";

    private static final String CORRECT_TEXT = "CORRECT_TEXT";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationLevelRepository notificationLevelRepository;

    @MockBean
    private UserRepository mockUserRepository;

    @MockBean
    private NotificationViewRepository mockNotificationViewRepository;

    @Mock
    private User mockBoss;

    @Mock
    private User mockSecretary;

    @Nested
    @DisplayName("createNotification tests")
    class CreateNotification {

        @Nested
        @DisplayName("With NotificationLevel name arg")
        class WithNotificationLevelNameAgr {

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "      "})
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"delete_all_notifications.sql"})
            @DisplayName("Test if createNotification returns false if level name is blank")
            void testWithBlankLevelName(String blank) {
                boolean success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, blank, false);

                verifyFail(success);
            }

            @Test
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"delete_all_notifications.sql"})
            @DisplayName("Test if createNotification returns false with unknown level name")
            void testWithUnknownLevelName() {
                boolean success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, UNKNOWN_NOTIFICATION_LEVEL, false);

                verifyFail(success);
            }

            @Test
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"create_false_notification_level.sql",
                                                                                    "delete_all_notifications.sql"})
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"delete_false_notification_level.sql"})
            @DisplayName("Test if createNotification returns false with known level name but without associated roles")
            void testWithKnownLevelNameWithoutRoles() {
                boolean success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, FALSE_NOTIFICATION_LEVEL, false);

                verifyFail(success);
            }

            @Test
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"delete_all_notifications.sql"})
            @DisplayName("Test if createNotification returns true with known level name and with associated roles")
            void testWithKnownLevelNameWithRole() {
                prepareUserRepositoryFindByRoles();
                boolean success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, ALL_ROLES_LEVEL, false);

                verifySuccess(success);
            }

        }

        @Nested
        @DisplayName("With NotificationLevel object arg")
        class WithNotificationLevelArg {

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "      "})
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"delete_all_notifications.sql"})
            @DisplayName("Test if createNotification returns false if notification level name is blank")
            void testWithBlankLevelName(String blank) {
                NotificationLevel level = NotificationLevel.builder().notificationLevelName(blank).build();
                boolean success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, level, false);

                verifyFail(success);
            }

            @Test
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"delete_all_notifications.sql"})
            @DisplayName("Test if createNotification returns false and rollback work if notification view creation fail")
            void testWithFailDuringNotificationViewCreation() {
                prepareUserRepositoryFindByRoles();
                given(mockNotificationViewRepository.save(any())).willThrow(new IllegalArgumentException());

                boolean success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, (NotificationLevel) null, false);

                verifyFail(success);
            }

            @Test
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"create_false_notification_level.sql",
                                                                                    "delete_all_notifications.sql"})
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"delete_false_notification_level.sql"})
            @DisplayName("Test if createNotification returns false if no roles is found for a correct notification level")
            void testWithNonRoleNotificationLevel() {
                NotificationLevel level = notificationLevelRepository.findByNotificationLevelName(FALSE_NOTIFICATION_LEVEL);
                log.debug("False level find = {}", level);
                boolean success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, level, false);

                verifyFail(success);
            }

            @ParameterizedTest
            @ValueSource(strings = {"\t", "", " ", "   "})
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"delete_all_notifications.sql"})
            @DisplayName("Test if createNotification returns false if black title or text is pass in parameter")
            void testWithBlankTitleOrText(String blank) {
                boolean success = notificationService.createNotification(blank, CORRECT_TEXT, (NotificationLevel) null, false);
                verifyFail(success);

                success = notificationService.createNotification(CORRECT_TITLE, blank, (NotificationLevel) null, false);
                verifyFail(success);

                success = notificationService.createNotification(blank, blank, (NotificationLevel) null, false);
                verifyFail(success);

                success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT,
                                                                 NotificationLevel.builder().notificationLevelName(blank).build(),
                                                                 false);
                verifyFail(success);
            }

            @Test
            @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"delete_all_notifications.sql"})
            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"delete_all_notifications.sql"})
            @DisplayName("Test if createNotification returns true and create notification for all users with null level")
            void testWithNullLevel() {
                prepareUserRepositoryFindByRoles();

                boolean success = notificationService.createNotification(CORRECT_TITLE, CORRECT_TEXT, (NotificationLevel) null, false);

                verifySuccess(success);

                verify(mockNotificationViewRepository, times(2)).save(any());
            }
        }
    }

    private void verifySuccess(boolean success) {
        assertThat(success).isTrue();
        verifyCreation();
    }

    private void verifyFail(boolean success) {
        assertThat(success).isFalse();
        verifyNoCreation();
    }

    private void prepareUserRepositoryFindByRoles() {
        List<User> users = new ArrayList<>();
        users.add(mockBoss);
        users.add(mockSecretary);

        given(mockUserRepository.findByRoles(any())).willReturn(users);
    }

    private void verifyCreation() {
        assertThat(notificationRepository.findByNotificationTitle(CORRECT_TITLE)).isNotEmpty().size().isEqualByComparingTo(1);
    }

    private void verifyNoCreation() {
        assertThat(notificationRepository.findByNotificationTitle(CORRECT_TITLE)).isEmpty();
    }

}
