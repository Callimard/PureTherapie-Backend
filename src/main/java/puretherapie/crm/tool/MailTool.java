package puretherapie.crm.tool;

import org.apache.commons.validator.routines.EmailValidator;

public class MailTool {

    private MailTool() {
    }

    // Tool methods.

    public static boolean isValidMail(String mail) {
        return EmailValidator.getInstance().isValid(mail);
    }

}
