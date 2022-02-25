package puretherapie.crm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "launchbreak")
public class LaunchBreakConfiguration {

    /**
     * The start when a launch break can be taken.
     */
    private String start;

    /**
     * The end when all launch breaks must finish.
     */
    private String end;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
