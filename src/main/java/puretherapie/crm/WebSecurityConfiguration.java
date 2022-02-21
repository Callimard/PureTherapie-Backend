package puretherapie.crm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import puretherapie.crm.authentication.CustomAuthenticationEntryPoint;

import static puretherapie.crm.WebConfiguration.IMAGES_URL;
import static puretherapie.crm.api.v1.agenda.controller.AgendaController.TECHNICIAN_FREE_TIME_SLOTS_URL;
import static puretherapie.crm.api.v1.appointment.controller.AppointmentController.APPOINTMENT_URL;
import static puretherapie.crm.api.v1.person.client.controller.ClientController.*;
import static puretherapie.crm.api.v1.person.technician.controller.TechnicianController.TECHNICIANS_URL;
import static puretherapie.crm.api.v1.product.aesthetic.care.controller.AestheticCareController.AESTHETIC_CARES_URL;
import static puretherapie.crm.api.v1.user.controller.UserController.USER_LOGIN_URL;
import static puretherapie.crm.api.v1.user.controller.UserController.USER_LOGOUT_URL;

@Configuration
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true)
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    // Constants.

    public static final String FRONT_END_ORIGIN = "https://localhost:4200";

    // Methods.

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureCsrf(http);
        configureAuthorizeRequests(http);
        configureSession(http);
        configureHttpBasic(http);
        configureLoginLogout(http);
    }

    private void configureCsrf(HttpSecurity http) throws Exception {
        http
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringAntMatchers(USER_LOGIN_URL, USER_LOGOUT_URL, CLIENTS_URL, APPOINTMENT_URL, IMAGES_URL);
    }

    private void configureSession(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    private void configureHttpBasic(HttpSecurity http) throws Exception {
        http.httpBasic().authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }

    private void configureAuthorizeRequests(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new ForwardedHeaderFilter(), ChannelProcessingFilter.class)
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.GET, PERSON_ORIGINS_URL).permitAll()
                .antMatchers(HttpMethod.POST, CLIENTS_URL).permitAll()
                .antMatchers(HttpMethod.GET, CLIENT_SEARCH_WITH_EMAIL_URL).permitAll()
                .antMatchers(HttpMethod.GET, CLIENT_SEARCH_WITH_PHONE_URL).permitAll()
                .antMatchers(HttpMethod.POST, APPOINTMENT_URL).permitAll()
                .antMatchers(HttpMethod.GET, TECHNICIANS_URL).permitAll()
                .antMatchers(HttpMethod.GET, AESTHETIC_CARES_URL).permitAll()
                .antMatchers(HttpMethod.GET, TECHNICIAN_FREE_TIME_SLOTS_URL).permitAll()
                .antMatchers(HttpMethod.GET, IMAGES_URL).permitAll()
                .anyRequest().authenticated();
    }

    private void configureLoginLogout(HttpSecurity http) throws Exception {
        http.formLogin().disable()
                .logout().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping(IMAGES_URL).allowCredentials(true).allowedOrigins(FRONT_END_ORIGIN).allowedMethods("*");
            }
        };
    }

}
