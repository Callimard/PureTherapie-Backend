package puretherapie.crm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import puretherapie.crm.authentication.CustomAuthenticationEntryPoint;

import static puretherapie.crm.WebConfiguration.IMAGES_URL;
import static puretherapie.crm.api.v1.appointment.controller.AppointmentController.API_V1_APPOINTMENT_URL;
import static puretherapie.crm.api.v1.client.controller.ClientController.API_V1_CLIENT_URL;
import static puretherapie.crm.api.v1.user.controller.UserController.*;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

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
                .csrfTokenRepository(new CookieCsrfTokenRepository())
                .ignoringAntMatchers(API_V1_USER_URL + USER_LOGIN, API_V1_USER_URL + USER_LOGOUT);
    }

    private void configureSession(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    private void configureHttpBasic(HttpSecurity http) throws Exception {
        http.httpBasic().authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }

    private void configureAuthorizeRequests(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, API_V1_CLIENT_URL).permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.POST, API_V1_USER_URL + USER_LOGIN).authenticated()
                .antMatchers(HttpMethod.POST, API_V1_USER_URL + USER_LOGOUT).authenticated()
                .antMatchers(HttpMethod.POST, API_V1_APPOINTMENT_URL).permitAll()
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

}
