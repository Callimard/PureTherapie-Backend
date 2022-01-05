package puretherapie.crm.api.v1.client.service;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import puretherapie.crm.api.v1.client.ClientInformation;
import puretherapie.crm.data.person.PersonOrigin;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.repository.PersonOriginRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.client.controller.ClientController.CLIENT_DOUBLOON_FIELD;
import static puretherapie.crm.api.v1.client.service.ClientRegistrationService.ID_CLIENT_FIELD;
import static puretherapie.crm.data.person.Person.*;
import static puretherapie.crm.tool.ControllerTool.ERROR_FIELD;
import static puretherapie.crm.tool.ControllerTool.SUCCESS_FIELD;

@SpringBootTest
@DisplayName("ClientRegistrationService tests")
class ClientRegistrationServiceTest {

    // Constants.

    public static final String DEFAULT_CORRECT_EMAIL = "g.r@domain.fr";

    public static final String DEFAULT_CORRECT_PHONE = "33607271445";

    // Variables.

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @MockBean
    private PersonOriginRepository mockPersonOriginRepo;

    @MockBean
    private ClientRepository mockClientRepo;

    @Mock
    private DataIntegrityViolationException mockDataIntegrityViolationException;

    @Mock
    private ConstraintViolationException mockConstraintViolationException;

    @Mock
    private Exception mockOtherCause;

    @Mock
    private Client mockClient;

    // Tests.

    @Test
    @DisplayName("Test if clientRegistration fail all null fields client information")
    void testClientRegistration() {
        ClientInformation emptyInfo = ClientInformation.builder().build();
        Map<String, Object> response = clientRegistrationService.clientRegistration(emptyInfo, true);

        verifyFailResponse(response, ERROR_FIELD);

        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) response.get(ERROR_FIELD);
        assertThat(errors).isNotNull()
                .containsKey(FIRST_NAME_FIELD)
                .containsKey(LAST_NAME_FIELD)
                .containsKey(EMAIL_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration success with only required fields client information and there is no doubloons are already client")
    void testClientRegistrationWithOnlyRequiredFields() {
        prepareClientRepoForSuccess();

        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration success")
    void testClientRegistrationWithNoPhoneClient() {
        prepareClientRepoForSuccess();

        ClientInformation c = createClientInfoWithNoPhone();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration success")
    void testClientRegistrationWithNoPhotoClient() {
        prepareClientRepoForSuccess();

        ClientInformation c = createClientInfoWithNoPhoto();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration with client info has doubloons and doubloonVerification is true fail")
    void testClientRegistrationWithDoubloonsButWithDoubloonsVerification() {
        prepareClientRepoForSuccess();

        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareDoubloonsFind();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);

        verifyFailResponse(response, CLIENT_DOUBLOON_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration with client info has doubloons and doubloonVerification is false success")
    void testClientRegistrationWithDoubloonsButNoDoubloonsVerification() {
        prepareClientRepoForSuccess();

        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareDoubloonsFind();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, false);
        verifySuccessResponse(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Guillaume", "guillaume", "GuiLLaume", "Léa", "Léa-Paci", "JeAn-Phillipe", "Phillipe", "Héèaa", "Hàbon"})
    @DisplayName("Test if clientRegistration success with correct firstName format")
    void testClientRegistrationWithCorrectFormat(String correctFirstName) {
        prepareClientRepoForSuccess();

        ClientInformation c = createClientInfoWithFirstName(correctFirstName);

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\t", "       ", "", "Léa-", "Lé~", "@", "&", "L\"éa", "Jean Phillipe",
                            "toooooooooooooooooooooooooooooooooooooooooooooooooooolong"})
    @DisplayName("Test if clientRegistration fail with not corrected firstName format")
    void testClientRegistrationWithWrongFirstName(String wrongFirstName) {
        ClientInformation c = createClientInfoWithFirstName(wrongFirstName);

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response, ERROR_FIELD);

        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) response.get(ERROR_FIELD);
        assertThat(errors).isNotNull().containsKey(FIRST_NAME_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration fail with already used email")
    void testClientRegistrationWithAlreadyUsedEmail() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareEmailAlreadyUsed();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response, ERROR_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration fail with already used phone")
    void testClientRegistrationWithAlreadyUsedPhone() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        preparePhoneAlreadyUsed();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response, ERROR_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration fail with undefined constraints violation")
    void testClientRegistrationWithUndefineViolationConstraint() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareUndefinedViolatedConstraint();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response, ERROR_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration fail with other cause than constraint violation")
    void testClientRegistrationWithOtherCauseThanConstraintViolation() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareOtherCauseThanConstraintViolation();

        Map<String, Object> response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response, ERROR_FIELD);
    }

    private void verifyFailResponse(Map<String, Object> response, String errorField) {
        assertThat(response).isNotNull().containsKey(errorField);
    }

    private void verifySuccessResponse(Map<String, Object> response) {
        assertThat(response).isNotNull().containsKey(SUCCESS_FIELD).containsKey(ID_CLIENT_FIELD);
    }

    private void prepareClientRepoForSuccess() {
        given(mockClient.getIdPerson()).willReturn(1);
        given(mockClientRepo.save(any())).willReturn(mockClient);
    }

    private void prepareOtherCauseThanConstraintViolation() {
        given(mockDataIntegrityViolationException.getCause()).willReturn(mockOtherCause);
        given(mockClientRepo.save(any())).willThrow(mockDataIntegrityViolationException);
    }

    private void prepareUndefinedViolatedConstraint() {
        given(mockConstraintViolationException.getConstraintName()).willReturn("undefined");
        given(mockDataIntegrityViolationException.getCause()).willReturn(mockConstraintViolationException);
        given(mockClientRepo.save(any())).willThrow(mockDataIntegrityViolationException);
    }

    private void prepareEmailAlreadyUsed() {
        given(mockConstraintViolationException.getConstraintName()).willReturn(UNIQUE_EMAIL_CONSTRAINTS);
        given(mockDataIntegrityViolationException.getCause()).willReturn(mockConstraintViolationException);
        given(mockClientRepo.save(any())).willThrow(mockDataIntegrityViolationException);
    }

    private void preparePhoneAlreadyUsed() {
        given(mockConstraintViolationException.getConstraintName()).willReturn(UNIQUE_PHONE_CONSTRAINTS);
        given(mockDataIntegrityViolationException.getCause()).willReturn(mockConstraintViolationException);
        given(mockClientRepo.save(any())).willThrow(mockDataIntegrityViolationException);
    }

    private void prepareGetNonPersonOrigin() {
        given(mockPersonOriginRepo.getNonePersonOrigin()).willReturn(new PersonOrigin(1, PersonOrigin.NONE_TYPE));
    }

    private void prepareNoDoubloonsFind() {
        given(mockClientRepo.findByFirstNameAndLastName(anyString(), anyString())).willReturn(null);
    }

    private void prepareDoubloonsFind() {
        List<Client> doubloons = new ArrayList<>();
        doubloons.add(createClientInfoWithPhone().buildClient(mockPersonOriginRepo));
        given(mockClientRepo.findByFirstNameAndLastName(anyString(), anyString())).willReturn(doubloons);
    }

    private ClientInformation createClientInfoMinimalRequirement() {
        return ClientInformation.builder()
                .firstName("Guillaume")
                .lastName("RAKOTOMALALA")
                .email(DEFAULT_CORRECT_EMAIL)
                .build();
    }

    private ClientInformation createClientInfoWithNoPhone() {
        ClientInformation info = createClientInfoMinimalRequirement();
        info.setPhone(null);
        return info;
    }

    private ClientInformation createClientInfoWithNoPhoto() {
        ClientInformation info = createClientInfoMinimalRequirement();
        info.setPhone(null);
        return info;
    }

    private ClientInformation createClientInfoWithPhone() {
        ClientInformation info = createClientInfoMinimalRequirement();
        info.setPhone(DEFAULT_CORRECT_PHONE);
        return info;
    }

    private ClientInformation createClientInfoWithFirstName(String firstName) {
        ClientInformation info = createClientInfoMinimalRequirement();
        info.setFirstName(firstName);
        return info;
    }
}
