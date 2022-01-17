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
import puretherapie.crm.api.v1.client.controller.dto.ClientDTO;
import puretherapie.crm.api.v1.client.controller.dto.ClientRegistrationFailDTO;
import puretherapie.crm.api.v1.client.controller.dto.ClientRegistrationResponseDTO;
import puretherapie.crm.data.person.PersonOrigin;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.data.person.client.repository.ClientRepository;
import puretherapie.crm.data.person.repository.PersonOriginRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static puretherapie.crm.data.person.Person.UNIQUE_EMAIL_CONSTRAINTS;
import static puretherapie.crm.data.person.Person.UNIQUE_PHONE_CONSTRAINTS;

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
        ClientDTO emptyInfo = ClientDTO.builder().build();
        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(emptyInfo, true);

        verifyFailResponse(response);

        ClientRegistrationFailDTO fail = (ClientRegistrationFailDTO) response;
        assertThat(fail.getFirstName()).isNotNull();
        assertThat(fail.getLastName()).isNotNull();
        assertThat(fail.getEmail()).isNotNull();
    }

    @Test
    @DisplayName("Test if clientRegistration success with only required fields client information and there is no doubloons are already client")
    void testClientRegistrationWithOnlyRequiredFields() {
        prepareClientRepoForSuccess();

        ClientDTO c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration success")
    void testClientRegistrationWithNoPhoneClient() {
        prepareClientRepoForSuccess();

        ClientDTO c = createClientInfoWithNoPhone();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration success")
    void testClientRegistrationWithNoPhotoClient() {
        prepareClientRepoForSuccess();

        ClientDTO c = createClientInfoWithNoPhoto();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration with client info has doubloons and doubloonVerification is true fail")
    void testClientRegistrationWithDoubloonsButWithDoubloonsVerification() {
        prepareClientRepoForSuccess();

        ClientDTO c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareDoubloonsFind();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);

        verifyFailResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration with client info has doubloons and doubloonVerification is false success")
    void testClientRegistrationWithDoubloonsButNoDoubloonsVerification() {
        prepareClientRepoForSuccess();

        ClientDTO c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareDoubloonsFind();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, false);
        verifySuccessResponse(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Guillaume", "guillaume", "GuiLLaume", "Léa", "Léa-Paci", "JeAn-Phillipe", "Phillipe", "Héèaa", "Hàbon"})
    @DisplayName("Test if clientRegistration success with correct firstName format")
    void testClientRegistrationWithCorrectFormat(String correctFirstName) {
        prepareClientRepoForSuccess();

        ClientDTO c = createClientInfoWithFirstName(correctFirstName);

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifySuccessResponse(response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\t", "       ", "", "Léa-", "Lé~", "@", "&", "L\"éa", "Jean Phillipe",
                            "toooooooooooooooooooooooooooooooooooooooooooooooooooolong"})
    @DisplayName("Test if clientRegistration fail with not corrected firstName format")
    void testClientRegistrationWithWrongFirstName(String wrongFirstName) {
        ClientDTO c = createClientInfoWithFirstName(wrongFirstName);

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response);

        ClientRegistrationFailDTO fail = (ClientRegistrationFailDTO) response;
        assertThat(fail.getFirstName()).isNotNull();
    }

    @Test
    @DisplayName("Test if clientRegistration fail with already used email")
    void testClientRegistrationWithAlreadyUsedEmail() {
        ClientDTO c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareEmailAlreadyUsed();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration fail with already used phone")
    void testClientRegistrationWithAlreadyUsedPhone() {
        ClientDTO c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        preparePhoneAlreadyUsed();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration fail with undefined constraints violation")
    void testClientRegistrationWithUndefineViolationConstraint() {
        ClientDTO c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareUndefinedViolatedConstraint();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response);
    }

    @Test
    @DisplayName("Test if clientRegistration fail with other cause than constraint violation")
    void testClientRegistrationWithOtherCauseThanConstraintViolation() {
        ClientDTO c = createClientInfoMinimalRequirement();

        prepareGetNonPersonOrigin();
        prepareNoDoubloonsFind();
        prepareOtherCauseThanConstraintViolation();

        ClientRegistrationResponseDTO response = clientRegistrationService.clientRegistration(c, true);
        verifyFailResponse(response);
    }

    private void verifyFailResponse(ClientRegistrationResponseDTO response) {
        assertThat(response).isNotNull();
        assertThat(response.isFailedResponse()).isTrue();
    }

    private void verifySuccessResponse(ClientRegistrationResponseDTO response) {
        assertThat(response).isNotNull();
        assertThat(response.isFailedResponse()).isFalse();
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

    private ClientDTO createClientInfoMinimalRequirement() {
        return ClientDTO.builder()
                .firstName("Guillaume")
                .lastName("RAKOTOMALALA")
                .email(DEFAULT_CORRECT_EMAIL)
                .build();
    }

    private ClientDTO createClientInfoWithNoPhone() {
        ClientDTO info = createClientInfoMinimalRequirement();
        info.setPhone(null);
        return info;
    }

    private ClientDTO createClientInfoWithNoPhoto() {
        ClientDTO info = createClientInfoMinimalRequirement();
        info.setPhone(null);
        return info;
    }

    private ClientDTO createClientInfoWithPhone() {
        ClientDTO info = createClientInfoMinimalRequirement();
        info.setPhone(DEFAULT_CORRECT_PHONE);
        return info;
    }

    private ClientDTO createClientInfoWithFirstName(String firstName) {
        ClientDTO info = createClientInfoMinimalRequirement();
        info.setFirstName(firstName);
        return info;
    }
}
