package puretherapie.crm.tool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("PhoneTool")
public class PhoneToolTest {

    @Nested
    @DisplayName("PhoneTool.formatPhone")
    class FormatPhone {

        @ParameterizedTest
        @ValueSource(strings = {"", "     ", " ", "\t", "\t\t", "hjgkhjgqhgqj", "{}", "$^qfqjqgçhqkf", "\0", "WRONG", "]--", "-(^%#)", "^@$ ", "_<(" +
                " -",
                                ")}![{[%@"})
        @NullSource
        @DisplayName("Test if formatPhone throws NotPhoneNumberException if the phone is null, blank or does not contains digit")
        void testFormatPhoneWithNotCorrectString(String wrongPhone) {
            assertThrows(PhoneTool.NotPhoneNumberException.class, () -> PhoneTool.formatPhone(wrongPhone));
        }

        @ParameterizedTest
        @ValueSource(strings = {"3065446015", "312104455", "20554", "215445", "2454445"})
        @DisplayName("Test if formatPone throws UnSupportedCountryPhoneException if the country code is not supported")
        void testFormatPhoneWithNotSupportedCountryCode(String unsupportedPhone) {
            assertThrows(PhoneTool.UnSupportedPhoneNumberException.class, () -> PhoneTool.formatPhone(unsupportedPhone));
        }

        @Nested
        @DisplayName("French number")
        class FrenchNumber {

            @ParameterizedTest
            @ValueSource(strings = {"33", "3360727144", "336", "3360727", "+33 6 07 27 14 4", "33a6a07a27a14a4"})
            @DisplayName("Test if french formatPhone throws ParseException if the phone number has not the correct length")
            void testFrenchFormatPhoneWihNotCorrectPhoneLength(String wrongFrenchPhone) {
                assertThrows(PhoneTool.FailToFormatPhoneNumber.class, () -> PhoneTool.formatPhone(wrongFrenchPhone));
            }

            @ParameterizedTest
            @ValueSource(strings = {"33607271440", "33678148411", "33145165003", "33788329142", "33 6 07 27 14 40", "33 6-07-27-14-40", "33-6 07-27" +
                    " 14_40", "+33 6 07 27 14 40", "33a6b07c27v14n40"})
            @DisplayName("Test if french formatPhone returns a corrected formatted phone number with correct phone number format")
            void testFrenchFormatPhoneWithCorrectPhoneLength(String correctFrenchPhone) {
                try {
                    String formattedPhone = PhoneTool.formatPhone(correctFrenchPhone);
                    verifyCorrectlyFormattedPhone(formattedPhone, PhoneTool.FRENCH_PHONE_FORMAT_REGEX);
                } catch (PhoneTool.UnSupportedPhoneNumberException | PhoneTool.NotPhoneNumberException | PhoneTool.FailToFormatPhoneNumber e) {
                    fail(e);
                }
            }
        }

        @Nested
        @DisplayName("US number")
        class USNumber {

            @ParameterizedTest
            @ValueSource(strings = {"1", "125633354", "16641", "1arza564at5a445a"})
            @DisplayName("Test if us formatPhone throws ParseException if the phone number has not the correct length")
            void testFrenchFormatPhoneWihNotCorrectPhoneLength(String wrongUSPhone) {
                assertThrows(PhoneTool.FailToFormatPhoneNumber.class, () -> PhoneTool.formatPhone(wrongUSPhone));
            }

            @ParameterizedTest
            @ValueSource(strings = {"12563335656", "1 255 256-5859", "+1 152-152-4556", "+1-356 215-6566", "1qs253sqf556sqqfx6664qqsfw", "+1 (254) " +
                    "569-8989"})
            @DisplayName("Test if us formatPhone returns a corrected formatted phone number with correct phone number format")
            void testFrenchFormatPhoneWithCorrectPhoneLength(String correctUSPhone) {
                try {
                    String formattedPhone = PhoneTool.formatPhone(correctUSPhone);
                    verifyCorrectlyFormattedPhone(formattedPhone, PhoneTool.US_PHONE_FORMAT_REGEX);
                } catch (PhoneTool.UnSupportedPhoneNumberException | PhoneTool.NotPhoneNumberException | PhoneTool.FailToFormatPhoneNumber e) {
                    fail(e);
                }
            }
        }

    }

    private void verifyCorrectlyFormattedPhone(String formattedPhone, String phoneRegex) {
        assertThat(formattedPhone).isNotNull();
        assertThat(formattedPhone).isNotBlank();
        assertThat(formattedPhone).matches(phoneRegex);
    }

}
