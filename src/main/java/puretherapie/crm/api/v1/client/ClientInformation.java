package puretherapie.crm.api.v1.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import puretherapie.crm.data.person.PersonOrigin;
import puretherapie.crm.data.person.PersonOriginRepository;
import puretherapie.crm.data.person.client.Client;
import puretherapie.crm.tool.PhoneTool;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static puretherapie.crm.data.person.Person.*;
import static puretherapie.crm.data.person.PersonOrigin.NONE_TYPE;
import static puretherapie.crm.data.person.client.Client.*;
import static puretherapie.crm.tool.MailTool.isValidMail;
import static puretherapie.crm.tool.PhoneTool.formatPhone;


@Builder
@Getter
@Setter
@AllArgsConstructor
public class ClientInformation {

    // Verification fields.

    public static final String PHOTO_FIELD = "photo";
    public static final String COMMENT_FIELD = "comment";
    public static final String TECHNICAL_COMMENT_FIELD = "technicalComment";
    public static final String FIRST_NAME_FIELD = "firstName";
    public static final String LAST_NAME_FIELD = "lastName";
    public static final String MAIL_FIELD = "mail";
    public static final String PHONE_FIELD = "phone";

    // Variables.

    private String photo;
    private String comment;
    private String technicalComment;
    private String firstName;
    private String lastName;
    private String mail;
    private boolean gender;
    private LocalDate birthday;
    private String phone;
    private long idOrigin;

    public void verify() throws ClientInformationVerificationException {
        Map<String, String> error = new HashMap<>();

        verifyPhoto(error);
        verifyComment(error);
        verifyTechnicalComment(error);
        verifyFirstName(error);
        verifyLastName(error);
        verifyMail(error);
        verifyPhoneNumber(error);

        if (!error.isEmpty())
            throw new ClientInformationVerificationException(error);
    }

    private void verifyComment(Map<String, String> error) {
        if (comment != null && comment.length() > COMMENT_MAX_LENGTH)
            error.put(COMMENT_FIELD, "Client comment too long (max length " + COMMENT_MAX_LENGTH + ")");
    }

    private void verifyTechnicalComment(Map<String, String> error) {
        if (technicalComment != null && technicalComment.length() > COMMENT_MAX_LENGTH)
            error.put(TECHNICAL_COMMENT_FIELD, "Client technical comment too long (max length " + COMMENT_MAX_LENGTH + ")");
    }

    private void verifyPhoto(Map<String, String> error) {
        if (photo != null && photo.isBlank())
            photo = NO_PHOTO;

        if (photo != null && photo.length() > PHONE_MAX_LENGTH)
            error.put(PHOTO_FIELD, "Photo path length too long (max length " + PHOTO_MAX_LENGTH + ")");

        // Nothing more for the moment (Maybe verify if the file exists)
    }

    private void verifyFirstName(Map<String, String> error) {
        if (firstName == null || firstName.isBlank() || doesNotMatchNameRegex(firstName, FIRST_NAME_MAX_LENGTH))
            error.put(FIRST_NAME_FIELD, "Client first name wrong format");
    }

    private void verifyLastName(Map<String, String> error) {
        if (lastName == null || lastName.isBlank() || doesNotMatchNameRegex(lastName, LAST_NAME_MAX_LENGTH))
            error.put(LAST_NAME_FIELD, "Client last name wrong format");
    }

    private void verifyMail(Map<String, String> error) {
        if (mail == null || mail.isBlank() || mail.length() > MAIL_MAX_LENGTH || !isValidMail(mail))
            error.put(MAIL_FIELD, "Client mail wrong format");
    }

    private void verifyPhoneNumber(Map<String, String> error) {
        if (phone == null || phone.isBlank()) {
            phone = NO_PHONE;
            return;
        }

        try {
            if (phone.length() > PHONE_MAX_LENGTH)
                error.put(PHONE_FIELD, "Client phone too long length (max " + PHONE_MAX_LENGTH + ")");

            phone = formatPhone(phone);
        } catch (PhoneTool.FailToFormatPhoneNumber e) {
            error.put(PHONE_FIELD, "Client phone wrong format");
        } catch (PhoneTool.UnSupportedPhoneNumberException e) {
            error.put(PHONE_FIELD, "Client phone country unsupported");
        } catch (PhoneTool.NotPhoneNumberException e) {
            error.put(PHONE_FIELD, "Client phone not a phone number");
        }
    }

    private boolean doesNotMatchNameRegex(String name, int maxLength) {
        return !name.matches("[a-zA-z][a-zA-z\\-àâéèêùûòôìîç]{0," + (maxLength - 2) + "}[a-zA-Zàâéèêùûòôìîç]");
    }


    public Client buildClient(PersonOriginRepository personOriginRepository) {
        return Client.builder()
                .photo(photo)
                .comment(comment)
                .technicalComment(technicalComment)
                .firstName(firstName)
                .lastName(lastName)
                .mail(mail)
                .gender(gender)
                .birthday(birthday)
                .phone(phone)
                .creationDate(OffsetDateTime.now())
                .personOrigin(getPersonOrigin(personOriginRepository))
                .build();
    }

    private PersonOrigin getPersonOrigin(PersonOriginRepository personOriginRepository) {
        PersonOrigin personOrigin;
        return (personOrigin = personOriginRepository.findByIdPersonOrigin(getIdOrigin())) == null ? noneOrigin(personOriginRepository) :
                personOrigin;
    }

    private PersonOrigin noneOrigin(PersonOriginRepository personOriginRepository) {
        return personOriginRepository.findByType(NONE_TYPE);
    }

    // Exceptions.

    public static class ClientInformationVerificationException extends Exception {

        private final Map<String, String> error;

        public ClientInformationVerificationException(Map<String, String> error) {
            super();
            this.error = error;
        }

        public Map<String, String> getError() {
            return error;
        }
    }
}
