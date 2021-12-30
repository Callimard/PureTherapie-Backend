package util;

import java.util.Base64;

public class TestTool {

    private TestTool() {
    }

    public static String btoaCredentials(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
