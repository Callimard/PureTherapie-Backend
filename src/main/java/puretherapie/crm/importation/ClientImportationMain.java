package puretherapie.crm.importation;

import puretherapie.crm.tool.PhoneTool;
import puretherapie.crm.tool.StringTool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ClientImportationMain {

    // Constants.

    public static final String CLIENTS_RESOURCES_PATH = "src/main/resources/clients-importation";
    public static final String CLIENTS_IMPORTATION_SQL_PATH = "src/main/resources/clients-importation/clients.sql";

    public static final String CSV_SEPARATOR = ";";

    private static final int NUMBER_FRENCH_PHONE_CHAR = 10;
    private static int noPhoneCounter = 0;

    private static int generatedEmailCounter = 0;

    public static final String DEFAULT_CLIENT_FIRST_NAME = "DEFAULT_CLIENT_FIRST_NAME";
    public static final String DEFAULT_CLIENT_LAST_NAME = "DEFAULT_CLIENT_LAST_NAME";
    public static final String DEFAULT_CLIENT_FIRST_DATE = "1996-01-01";

    public static final String DEFAULT_TECHNICIAN_VARIABLE = "@defaultTech";

    private static final Set<String> PHONE_ADDED = new HashSet<>();

    private static int clientCounter = 0;

    // Main.

    public static void main(String[] args) {
        String[] files = new String[]{CLIENTS_RESOURCES_PATH + "/bd-agenda1.csv", CLIENTS_RESOURCES_PATH + "/bd-agenda2.csv"};

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
            writer.flush();

            for (String file : files) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] split = line.split(CSV_SEPARATOR);

                        String firstName;
                        if (split.length >= 1)
                            firstName = verifyFirstName(split[0]);
                        else
                            firstName = verifyFirstName(null);

                        String lastName;
                        if (split.length >= 2)
                            lastName = verifyLastName(verifyLastName(split[1]));
                        else
                            lastName = verifyLastName(null);

                        String phone;
                        if (split.length >= 3)
                            phone = verifyPhone(split[2]);
                        else
                            phone = verifyPhone(null);

                        String date;
                        if (split.length >= 4)
                            date = verifyFirstAppointmentDate(split[3]);
                        else
                            date = null;

                        writer.write(generatePerson(firstName, lastName, phone, date));
                        writer.newLine();
                        writer.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Methods.

    private static String selectDefaultTechnician() {
        return """
                    SELECT puretherapie.Person.idPerson INTO %s
                    FROM puretherapie.Person
                    WHERE firstName = 'default';
                """.formatted(DEFAULT_TECHNICIAN_VARIABLE);
    }

    private static String verifyFirstName(String firstName) {
        if (firstName == null || firstName.isBlank())
            return DEFAULT_CLIENT_FIRST_NAME;

        return StringTool.removeRemainingSpaces(firstName.toLowerCase());
    }

    private static String verifyLastName(String lastName) {
        if (lastName == null || lastName.isBlank())
            return DEFAULT_CLIENT_LAST_NAME;

        return StringTool.removeRemainingSpaces(lastName.toLowerCase());
    }

    private static String verifyPhone(String phone) {
        try {
            if (phone == null || phone.isBlank()) {
                phone = generateDefaultPhone();
                return PhoneTool.formatPhone("33" + phone);
            } else {
                phone = "33" + phone;

                if (!PHONE_ADDED.add(phone))
                    phone = "33" + generateDefaultPhone();

                return PhoneTool.formatPhone(phone);
            }
        } catch (PhoneTool.UnSupportedPhoneNumberException | PhoneTool.NotPhoneNumberException | PhoneTool.FailToFormatPhoneNumber e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String generateDefaultPhone() {
        String noPhoneCounterString = String.valueOf(noPhoneCounter++);
        int remainingChar = NUMBER_FRENCH_PHONE_CHAR - noPhoneCounterString.length();
        return "0".repeat(Math.max(0, remainingChar - 1)) + noPhoneCounterString;
    }

    private static String verifyFirstAppointmentDate(String date) {
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

    private static String generatePerson(String firstName, String lastName, String phone, String date) {
        int currentClientCounter = clientCounter++;

        String clientCreation = """
                INSERT INTO puretherapie.Person (puretherapie.Person.persontype, puretherapie.Person.firstname, puretherapie.Person.lastname,
                                                 puretherapie.Person.email, puretherapie.Person.gender,
                                                 puretherapie.Person.phone, puretherapie.Person.creationdate, puretherapie.Person.idPersonOrigin)
                VALUES ('C', '%s', '%s', '%s', 0, '%s', NOW(), 1);
                SET @person_%s = LAST_INSERT_ID();
                INSERT INTO puretherapie.Client (puretherapie.Client.idPerson)
                VALUES (@person_%s);
                """.formatted(firstName, lastName, generateEmail(), phone, currentClientCounter, currentClientCounter);

        String firstApp;
        if (date != null) {
            firstApp = """
                    INSERT INTO puretherapie.Appointment (puretherapie.Appointment.idAestheticCare, puretherapie.Appointment.idClient,
                                                          puretherapie.Appointment.idTechnician, puretherapie.Appointment.day, puretherapie.Appointment.time,
                                                          puretherapie.Appointment.finalized)
                    VALUES (1, @person_%s, %s, '%s', '09:00:00', 1);
                    """.formatted(currentClientCounter, DEFAULT_TECHNICIAN_VARIABLE, date);
            return clientCreation + firstApp;
        } else {
            return clientCreation;
        }
    }

    private static String generateEmail() {
        return "default.email" + (generatedEmailCounter++) + "@default.fr";
    }
}
