package puretherapie.crm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "forgotten-password")
public class ForgottenPasswordConfiguration {

    private String sendFrom;

    private String sendTo;

    private String frontResetPasswordUrl;

    public String getSendFrom() {
        return sendFrom;
    }

    public void setSendFrom(String sendFrom) {
        this.sendFrom = sendFrom;
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public String getFrontResetPasswordUrl() {
        return frontResetPasswordUrl;
    }

    public void setFrontResetPasswordUrl(String frontResetPasswordUrl) {
        this.frontResetPasswordUrl = frontResetPasswordUrl;
    }
}
