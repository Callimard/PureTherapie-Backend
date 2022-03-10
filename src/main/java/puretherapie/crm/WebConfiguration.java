package puretherapie.crm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Slf4j
@Configuration
public class WebConfiguration extends WebMvcConfigurationSupport {

    public static final String IMAGES_URL = "/images/**";
    public static final String UPLOADS_URL = "/uploads/**";

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(IMAGES_URL)
                .addResourceLocations("file:/images/");
        registry.addResourceHandler(UPLOADS_URL)
                .addResourceLocations("file:/uploads/");
    }
}
