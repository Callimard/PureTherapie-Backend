package puretherapie.crm.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ServiceTool {

    private ServiceTool() {
    }

    // Constants.

    public static final String DATA_DIRECTORY_PATH = "src/main/resources/data";

    // Tool methods.

    public static Map<String, String> generateError(String errorName, String errorMessage) {
        Map<String, String> errors = new HashMap<>();
        errors.put(errorName, errorMessage);
        return errors;
    }

    public static void createDataDirectory() throws IOException {
        Files.createDirectories(Path.of(DATA_DIRECTORY_PATH));
    }

    // Exceptions class.

    public static class ServiceException extends RuntimeException {

        private final Map<String, String> errors;

        public ServiceException(String message, Map<String, String> errors) {
            super(message);
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }
}
