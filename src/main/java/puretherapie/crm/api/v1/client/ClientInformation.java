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

import static puretherapie.crm.data.person.Person.*;
import static puretherapie.crm.data.person.PersonOrigin.NONE_TYPE;
import static puretherapie.crm.data.person.client.Client.COMMENT_MAX_LENGTH;
import static puretherapie.crm.data.person.client.Client.NO_PHOTO;
import static puretherapie.crm.tool.MailTool.isValidMail;
import static puretherapie.crm.tool.PhoneTool.formatPhone;


@Builder
@Getter
@Setter
@AllArgsConstructor
public class ClientInformation {

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

    public void verify()
            throws ClientPhotoException, ClientCommentException, ClientTechnicalCommentException, ClientFirstNameException, ClientLastNameException,
                   ClientMailException, ClientPhoneException {
        verifyPhoto();
        verifyComment();
        verifyTechnicalComment();
        verifyFirstName();
        verifyLastName();
        verifyMail();
        verifyPhoneNumber();
    }

    private void verifyComment() throws ClientCommentException {
        if (comment != null && comment.length() > COMMENT_MAX_LENGTH)
            throw new ClientCommentException("ClientComment too long (max length " + COMMENT_MAX_LENGTH + ")");
    }

    private void verifyTechnicalComment() throws ClientTechnicalCommentException {
        if (technicalComment != null && technicalComment.length() > COMMENT_MAX_LENGTH)
            throw new ClientTechnicalCommentException("ClientTechnicalComment too long (max length " + COMMENT_MAX_LENGTH + ")");
    }

    private void verifyPhoto() throws ClientPhotoException {
        if (photo != null && photo.isBlank())
            photo = NO_PHOTO;

        if (photo != null && photo.length() > PHONE_MAX_LENGTH)
            throw new ClientPhotoException();

        // Nothing more for the moment (Maybe verify if the file exists)
    }

    private void verifyFirstName() throws ClientFirstNameException {
        if (firstName == null || firstName.isBlank() || doesNotMatchNameRegex(firstName, FIRST_NAME_MAX_LENGTH))
            throw new ClientFirstNameException("ClientFirstName wring format");
    }

    private void verifyLastName() throws ClientLastNameException {
        if (lastName == null || lastName.isBlank() || doesNotMatchNameRegex(lastName, LAST_NAME_MAX_LENGTH))
            throw new ClientLastNameException("ClientFirstName wring format");
    }

    private void verifyMail() throws ClientMailException {
        if (mail == null || mail.isBlank() || mail.length() > MAIL_MAX_LENGTH || !isValidMail(mail))
            throw new ClientMailException("ClientMail wrong format");
    }

    private void verifyPhoneNumber() throws ClientPhoneException {
        if (phone == null || phone.isBlank()) {
            phone = NO_PHONE;
            return;
        }

        try {
            if (phone.length() > PHONE_MAX_LENGTH)
                throw new ClientPhoneException("ClientPhone too long length (max " + PHONE_MAX_LENGTH + ")");

            phone = formatPhone(phone);
        } catch (PhoneTool.FailToFormatPhoneNumber e) {
            throw new ClientPhoneException("ClientPhone wrong format");
        } catch (PhoneTool.UnSupportedPhoneNumberException e) {
            throw new ClientPhoneException("ClientPhone country unsupported");
        } catch (PhoneTool.NotPhoneNumberException e) {
            throw new ClientPhoneException("ClientPhone not a phone number");
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

    public static class ClientPhoneException extends Exception {
        public ClientPhoneException(String message) {
            super(message);
        }
    }

    public static class ClientMailException extends Exception {
        public ClientMailException(String message) {
            super(message);
        }
    }

    public static class ClientFirstNameException extends Exception {
        public ClientFirstNameException(String message) {
            super(message);
        }
    }

    public static class ClientLastNameException extends Exception {
        public ClientLastNameException(String message) {
            super(message);
        }
    }

    public static class ClientPhotoException extends Exception {
    }

    public static class ClientCommentException extends Exception {
        public ClientCommentException(String message) {
            super(message);
        }
    }

    public static class ClientTechnicalCommentException extends Exception {
        public ClientTechnicalCommentException(String message) {
            super(message);
        }
    }

}
