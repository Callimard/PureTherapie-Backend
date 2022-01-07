package puretherapie.crm.tool;

import java.util.HashMap;
import java.util.Map;

public class ServiceTool {

    private ServiceTool() {
    }

    // Tool methods.

    public static Map<String, String> generateErrors(String errorName, String errorMessage) {
        Map<String, String> errors = new HashMap<>();
        errors.put(errorName, errorMessage);
        return errors;
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
