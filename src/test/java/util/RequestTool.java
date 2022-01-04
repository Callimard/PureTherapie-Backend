package util;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RequestTool {

    // Constants.

    public static final String JSON_CONTENT_TYPE = "application/json";

    // Constructors.

    private RequestTool() {}

    public static MockHttpServletRequestBuilder httpPostJson(String url) {
        return post(url).contentType(JSON_CONTENT_TYPE);
    }

}
