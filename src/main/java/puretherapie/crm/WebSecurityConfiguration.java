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
import puretherapie.crm.authentication.CustomAuthenticationEntryPoint;

import static puretherapie.crm.api.v1.client.controller.ClientController.API_V1_CLIENT_URL;
import static puretherapie.crm.api.v1.user.controller.UserController.*;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureCors(http);
        configureCsrf(http);
        configureAuthorizeRequests(http);
        configureSession(http);
        configureHttpBasic(http);
        configureLoginLogout(http);
        configureExceptionHandling(http);
    }

    private void configureCors(HttpSecurity http) throws Exception {
        http.cors().disable();
    }

    private void configureCsrf(HttpSecurity http) throws Exception {
        http.csrf().disable();
    }

    private void configureSession(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    private void configureHttpBasic(HttpSecurity http) throws Exception {
        http.httpBasic();
    }

    private void configureAuthorizeRequests(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, API_V1_CLIENT_URL).permitAll()
                .antMatchers(HttpMethod.POST, API_V1_USER_URL + USER_LOGIN).authenticated()
                .antMatchers(HttpMethod.POST, API_V1_USER_URL + USER_LOGOUT).authenticated()
                .anyRequest().authenticated();
    }

    private void configureLoginLogout(HttpSecurity http) throws Exception {
        http.formLogin().disable()
                .logout().disable();
    }

    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
