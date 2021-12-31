package puretherapie.crm.tool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MailTool")
public class MailToolTest {

    @Nested
    @DisplayName("MailTool.isValidMail")
    class IsValidMail {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\t\t\n", "\0", "\n", "aggkhga", "kjsg", "_#{", "{%$-_<)", "^_@()$_", "[{(*", "_!{ ^^",
                                ".g.r@h.fr", "g.r.@h.fr", "g.h@.h.fr", "g.h@h.fr.", "g.h@h;f", "a;a@h.fr", "g.h@g"})
        @DisplayName("Test if isValidMail returns false for a non correct mail")
        void testIsValidMailWithWrongMail(String wrongMail) {
            boolean isValid = MailTool.isValidMail(wrongMail);
            assertThat(isValid).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"g.h@h.fr", "ali.baba@hotmail.Fr", "troll.foire@foire.fr", "a@ankama.fr", "HAHAHA@hotmail.fr"})
        @DisplayName("Test if isValidMail returns true for a correct mail")
        void testIsValidMailWithCorrectMain(String correctMail) {
            boolean isValid = MailTool.isValidMail(correctMail);
            assertThat(isValid).isTrue();
        }
    }

}
