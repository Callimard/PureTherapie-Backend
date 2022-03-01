package puretherapie.crm;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebConfiguration extends WebMvcConfigurationSupport {

    public static final String IMAGES_URL = "/images/**";

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userDir = System.getProperty("user.dir");
        registry.addResourceHandler(IMAGES_URL)
                .addResourceLocations("file:" + userDir + "/images/");
    }
}
