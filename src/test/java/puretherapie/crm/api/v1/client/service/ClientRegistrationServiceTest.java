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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import puretherapie.crm.api.v1.client.ClientInformation;
import puretherapie.crm.data.person.PersonOrigin;
import puretherapie.crm.data.person.repository.PersonOriginRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.api.v1.client.controller.ClientController.CLIENT_DOUBLOON_FIELD;
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

    @MockBean
    private PersonOriginRepository mockPersonOriginRepo;

    @MockBean
    private ClientRepository mockClientRepo;

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @Mock
    private DataIntegrityViolationException mockDataIntegrityViolationException;

    @Mock
    private ConstraintViolationException mockConstraintViolationException;

    @Mock
    private Exception mockOtherCause;

    // Tests.

    @Test
    @DisplayName("Test if clientRegistration returns 400 all null fields client information")
    void testClientRegistration() {
        ClientInformation emptyInfo = ClientInformation.builder().build();
        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(emptyInfo, true);

        Map<String, Object> body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body).isNotNull().containsKey(ERROR_FIELD);

        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) body.get(ERROR_FIELD);
        assertThat(errors).isNotNull()
                .containsKey(FIRST_NAME_FIELD)
                .containsKey(LAST_NAME_FIELD)
                .containsKey(EMAIL_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration returns 200 with only required fields client information and there is no doubloons are already client")
    void testClientRegistrationWithOnlyRequiredFields() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration returns 200")
    void testClientRegistrationWithNoPhoneClient() {
        ClientInformation c = prepareClientInfoWithNoPhone();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration returns 200")
    void testClientRegistrationWithNoPhotoClient() {
        ClientInformation c = createClientInfoWithNoPhoto();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration with client info has doubloons and doubloonVerification is true returns 400")
    void testClientRegistrationWithDoubloonsButWithDoubloonsVerification() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareDoubloonsFind();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);

        Map<String, Object> body = verifyIsBadRequest(response, HttpStatus.BAD_REQUEST, CLIENT_DOUBLOON_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration with client info has doubloons and doubloonVerification is false returns 200")
    void testClientRegistrationWithDoubloonsButNoDoubloonsVerification() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareDoubloonsFind();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, false);
        verifySuccessResponse(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Guillaume", "guillaume", "GuiLLaume", "Léa", "Léa-Paci", "JeAn-Phillipe", "Phillipe", "Héèaa", "Hàbon"})
    @DisplayName("Test if clientRegistration returns 200 with correct firstName format")
    void testClientRegistrationWithCorrectFormat(String correctFirstName) {
        ClientInformation c = createClientInfoWithFirstName(correctFirstName);

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\t", "       ", "", "Léa-", "Lé~", "@", "&", "L\"éa", "Jean Phillipe",
                            "toooooooooooooooooooooooooooooooooooooooooooooooooooolong"})
    @DisplayName("Test if clientRegistration returns 400 with not corrected firstName format")
    void testClientRegistrationWithWrongFirstName(String wrongFirstName) {
        ClientInformation c = createClientInfoWithFirstName(wrongFirstName);

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        Map<String, Object> body = verifyIsBadRequest(response, HttpStatus.BAD_REQUEST, ERROR_FIELD);

        @SuppressWarnings("unchecked") Map<String, String> errors = (Map<String, String>) body.get(ERROR_FIELD);
        assertThat(errors).isNotNull().containsKey(FIRST_NAME_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration returns 400 with already used email")
    void testClientRegistrationWithAlreadyUsedEmail() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareEmailAlreadyUsed();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        verifyIsBadRequest(response, HttpStatus.BAD_REQUEST, ERROR_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration returns 400 with already used phone")
    void testClientRegistrationWithAlreadyUsedPhone() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        preparePhoneAlreadyUsed();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        verifyIsBadRequest(response, HttpStatus.BAD_REQUEST, ERROR_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration returns 400 with undefined constraints violation")
    void testClientRegistrationWithUndefineViolationConstraint() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareUndefinedViolatedConstraint();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        verifyIsBadRequest(response, HttpStatus.BAD_REQUEST, ERROR_FIELD);
    }

    @Test
    @DisplayName("Test if clientRegistration returns 400 with other cause than constraint violation")
    void testClientRegistrationWithOtherCauseThanConstraintViolation() {
        ClientInformation c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareOtherCauseThanConstraintViolation();

        ResponseEntity<Map<String, Object>> response = clientRegistrationService.noTransactionalClientRegistration(c, true);
        verifyIsBadRequest(response, HttpStatus.BAD_REQUEST, ERROR_FIELD);
    }

    private void verifySuccessResponse(ResponseEntity<Map<String, Object>> response) {
        verifyIsBadRequest(response, HttpStatus.OK, SUCCESS_FIELD);
    }

    private Map<String, Object> verifyIsBadRequest(ResponseEntity<Map<String, Object>> response, HttpStatus badRequest, String errorField) {
        assertThat(response.getStatusCode()).isEqualTo(badRequest);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull().containsKey(errorField);
        return body;
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

    private ClientInformation prepareClientInfoWithNoPhone() {
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
