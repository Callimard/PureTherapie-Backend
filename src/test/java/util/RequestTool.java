package util;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static util.TestTool.btoaCredentials;

public class RequestTool {

    // Constants.

    public static final String JSON_CONTENT_TYPE = "application/json";

    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Constructors.

    private RequestTool() {
    }

    public static MockHttpServletRequestBuilder httpPostJson(String url) {
        return post(url).contentType(JSON_CONTENT_TYPE);
    }

    public static MockHttpServletRequestBuilder httpPostJsonWithAuthorization(String url, String username, String password) {
        return httpPostJson(url).header(AUTHORIZATION_HEADER, basicAuthorization(username, password));
    }

    public static String basicAuthorization(String username, String password) {
        return "Basic " + btoaCredentials(username, password);
    }

}
