package puretherapie.crm.importation;

import lombok.extern.slf4j.Slf4j;
import puretherapie.crm.tool.PhoneTool;
import puretherapie.crm.tool.StringTool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ClientImportationMain {

    // Constants.

    public static final String CLIENTS_RESOURCES_PATH = "src/main/resources/clients-importation";
    public static final String CLIENTS_IMPORTATION_SQL_PATH = "sql/init/total_clients.sql";

    public static final String CSV_SEPARATOR = ";";

    private static final int NUMBER_FRENCH_PHONE_CHAR = 10;
    private static int noPhoneCounter = 0;

    private static int generatedEmailCounter = 0;

    public static final String DEFAULT_CLIENT_FIRST_NAME = "DEFAULT_CLIENT_FIRST_NAME";
    public static final String DEFAULT_CLIENT_LAST_NAME = "DEFAULT_CLIENT_LAST_NAME";

    public static final String DEFAULT_TECHNICIAN_VARIABLE = "@defaultTech";
    public static final String BUNDLE_PACKAGE = "@bundle_package";
    public static final String ONE_TIME_PAYMENT_TYPE = "@one_time_pt";
    public static final String CREDIT_CARD_MEANS_PAYMENT = "@cr_means_payment";
    public static final String AC_DISCOVERY = "@ac_discovery";

    private static final Set<String> PHONE_ADDED = new HashSet<>();

    private static int clientCounter = 0;

    // Main

    public static void main(String[] args) {
        String[] files = new String[]{CLIENTS_RESOURCES_PATH + "/bdfin_full.csv", CLIENTS_RESOURCES_PATH + "/bd-agenda-full.csv"};

        if (Files.exists(Path.of(CLIENTS_IMPORTATION_SQL_PATH))) {
            try {
                Files.deleteIfExists(Path.of(CLIENTS_IMPORTATION_SQL_PATH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CLIENTS_IMPORTATION_SQL_PATH, true))) {
            writer.write(selectDefaultTechnician());
            writer.newLine();
            writer.write(selectBundlePackage());
            writer.newLine();
            writer.write(selectOneTimePaymentType());
            writer.newLine();
            writer.write(selectCreditCardMeansOfPayment());
            writer.newLine();
            writer.write(selectACDiscovery());
            writer.newLine();
            writer.flush();

            for (String file : files) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] split = line.split(CSV_SEPARATOR);

                        String lastName = extractLastName(split);
                        String firstName = extractFirstName(split);
                        String phone = extractPhone(split);
                        String date = extractDate(split);
                        String stock = extractStock(split);
                        String remainingPayment = extractRemainingPayment(split);

                        writer.write(generatePerson(firstName, lastName, phone, date, stock, remainingPayment));
                        writer.newLine();
                        writer.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String selectDefaultTechnician() {
        return """
                SELECT puretherapie.Person.idPerson INTO %s
                FROM puretherapie.Person
                WHERE firstName = 'default';
                """.formatted(DEFAULT_TECHNICIAN_VARIABLE);
    }

    private static String selectBundlePackage() {
        return """
                SELECT puretherapie.Bundle.idBundle INTO %s
                FROM puretherapie.Bundle
                WHERE name = 'Package';
                """.formatted(BUNDLE_PACKAGE);
    }

    private static String selectOneTimePaymentType() {
        return """
                SELECT idPaymentType INTO %s
                FROM PaymentType
                WHERE name = 'ONT_TIME';
                """.formatted(ONE_TIME_PAYMENT_TYPE);
    }

    private static String selectCreditCardMeansOfPayment() {
        return """
                SELECT idMeansOfPayment INTO %s
                FROM MeansOfPayment
                WHERE name = 'Carte bancaire';
                """.formatted(CREDIT_CARD_MEANS_PAYMENT);
    }

    private static String selectACDiscovery() {
        return """
                SELECT idAestheticCare INTO %s
                FROM AestheticCare
                WHERE name = 'Soin découverte';
                """.formatted(AC_DISCOVERY);
    }

    private static String extractLastName(String[] split) {
        String lastName;
        if (split.length >= 1)
            lastName = verifyLastName(verifyLastName(split[0]));
        else
            lastName = verifyLastName(null);
        return lastName;
    }

    private static String verifyLastName(String lastName) {
        if (lastName == null || lastName.isBlank())
            return DEFAULT_CLIENT_LAST_NAME;

        return StringTool.removeRemainingSpaces(lastName.toLowerCase());
    }

    private static String extractFirstName(String[] split) {
        String firstName;
        if (split.length >= 2)
            firstName = verifyFirstName(split[1]);
        else
            firstName = verifyFirstName(null);
        return firstName;
    }

    private static String verifyFirstName(String firstName) {
        if (firstName == null || firstName.isBlank())
            return DEFAULT_CLIENT_FIRST_NAME;

        return StringTool.removeRemainingSpaces(firstName.toLowerCase());
    }

    private static String extractPhone(String[] split) {
        String phone;
        if (split.length >= 3)
            phone = verifyPhone(split[2]);
        else
            phone = verifyPhone(null);
        return phone;
    }

    private static String verifyPhone(String phone) {
        try {
            if (phone == null || phone.isBlank()) {
                phone = generateDefaultPhone();
                return PhoneTool.formatPhone("33" + phone);
            } else {
                phone = "33" + phone;

                /*if (!PHONE_ADDED.add(phone))
                    phone = "33" + generateDefaultPhone();*/

                return PhoneTool.formatPhone(phone);
            }
        } catch (PhoneTool.UnSupportedPhoneNumberException | PhoneTool.NotPhoneNumberException | PhoneTool.FailToFormatPhoneNumber e) {
            log.error("Phone format error", e);
            phone = generateDefaultPhone();
            try {
                return PhoneTool.formatPhone("33" + phone);
            } catch (PhoneTool.UnSupportedPhoneNumberException | PhoneTool.NotPhoneNumberException | PhoneTool.FailToFormatPhoneNumber ex) {
                log.error("BIG Phone format error", e);
                return null;
            }
        }
    }

    private static String extractDate(String[] split) {
        String date;
        if (split.length >= 4)
            date = verifyAppointmentDate(split[3]);
        else
            date = null;
        return date;
    }

    private static String verifyAppointmentDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }

        date = date.replace("/", "-");

        if (!date.contains("-")) {
            date = "01-01-20" + date;
        }

        String[] split = date.split("-");
        String tmpYear = split[2];
        split[2] = split[0];
        split[0] = tmpYear;

        return split[0] + "-" + split[1] + "-" + split[2];
    }

    private static String extractStock(String[] split) {
        String stock;
        if (split.length >= 5)
            stock = verifyStock(split[4]);
        else
            stock = null;

        return stock;
    }

    private static String verifyStock(String s) {
        try {
            if (s != null && !s.isBlank()) {
                long stock = Long.parseLong(s);
                if (stock < 0)
                    return null;
                else
                    return s;
            } else
                return null;
        } catch (Exception e) {
            log.error("Stock error", e);
            return null;
        }
    }

    private static String extractRemainingPayment(String[] split) {
        String remainingPayment;
        if (split.length >= 6)
            remainingPayment = verifyRemainingPayment(split[5]);
        else
            remainingPayment = null;
        return remainingPayment;
    }

    private static String verifyRemainingPayment(String s) {
        try {
            if (s != null && !s.isBlank()) {
                long remainingPayment = Long.parseLong(s);
                if (remainingPayment < 0)
                    return null;
                else
                    return s;
            } else
                return null;
        } catch (Exception e) {
            log.error("Remaining payment error", e);
            return null;
        }
    }

    private static String generateDefaultPhone() {
        String noPhoneCounterString = String.valueOf(noPhoneCounter++);
        int remainingChar = NUMBER_FRENCH_PHONE_CHAR - noPhoneCounterString.length();
        return "0".repeat(Math.max(0, remainingChar - 1)) + noPhoneCounterString;
    }

    private static String generatePerson(String firstName, String lastName, String phone, String date, String stock, String remainingPayment) {
        int currentClientCounter = clientCounter++;
        StringBuilder builder = new StringBuilder();

        builder.append(generateClientSql(firstName, lastName, phone, currentClientCounter));

        if (date != null) {
            builder.append(generateClientAppointmentSql(date, currentClientCounter));
        }

        if (remainingPayment != null) {
            builder.append(generateClientBillAndBundlePurchase(currentClientCounter, remainingPayment));

            if (stock != null) {
                builder.append(generateClientUpdateStock(currentClientCounter, stock));
            }
        }

        return builder.toString();
    }

    private static String generateClientSql(String firstName, String lastName, String phone, int currentClientCounter) {
        return """
                INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                                 puretherapie.Person.email, puretherapie.Person.gender,
                                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
                VALUES ('C', '%s', '%s', '%s', 0, '%s', NOW(), 1);
                SET @person_%s = LAST_INSERT_ID();
                INSERT INTO puretherapie.Client (puretherapie.Client.idPerson)
                VALUES (@person_%s);
                """.formatted(firstName, lastName, generateEmail(), phone, currentClientCounter, currentClientCounter);
    }

    private static String generateEmail() {
        return "default.email" + (generatedEmailCounter++) + "@default.fr";
    }

    private static String generateClientAppointmentSql(String date, int currentClientCounter) {
        String firstApp;
        firstApp = """
                INSERT INTO puretherapie.Appointment (puretherapie.Appointment.idAestheticCare, puretherapie.Appointment.idClient,
                                                      puretherapie.Appointment.idTechnician, puretherapie.Appointment.day, puretherapie.Appointment.time,
                                                      puretherapie.Appointment.finalized)
                VALUES (1, @person_%s, %s, '%s', '09:00:00', 1);
                """.formatted(currentClientCounter, DEFAULT_TECHNICIAN_VARIABLE, date);
        return firstApp;
    }

    private static String generateClientBillAndBundlePurchase(int currentClientCounter, String remainingPayment) {
        return """
                INSERT INTO Bill (basePrice, purchasePrice, creationDate, idClient, idPaymentType)
                VALUES (380.00, 380.00, '1996-09-03 09:00:00', @person_%s, %s);
                SET @bill_%s = LAST_INSERT_ID();
                                
                INSERT INTO BundlePurchase (idClient, idBundle, idBill)
                VALUES (@person_%s, %s, @bill_%s);
                SET @bundle_purchase_%s = LAST_INSERT_ID();
                                
                INSERT INTO Stock (remainingQuantity, idAestheticCare, idBundlePurchase)
                VALUES (0, %s, @bundle_purchase_%s);
                SET @stock_%s = LAST_INSERT_ID();
                                
                INSERT INTO Payment (amountPaid, paymentDate, idBill, idMeansOfPayment)
                VALUES (%s, '1996-09-03 09:00:00', @bill_%s, %s);
                """.formatted(currentClientCounter, ONE_TIME_PAYMENT_TYPE, currentClientCounter,
                              currentClientCounter, BUNDLE_PACKAGE, currentClientCounter, currentClientCounter,
                              AC_DISCOVERY, currentClientCounter, currentClientCounter,
                              (380 - Long.parseLong(remainingPayment)), currentClientCounter, CREDIT_CARD_MEANS_PAYMENT);
    }

    private static String generateClientUpdateStock(int currentClientCounter, String stock) {
        return """
                UPDATE Stock
                SET remainingQuantity = %s
                WHERE idStock = @stock_%s;
                """.formatted(stock, currentClientCounter);
    }

}
