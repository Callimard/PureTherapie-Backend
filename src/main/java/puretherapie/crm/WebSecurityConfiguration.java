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
import puretherapie.crm.person.user.data.User;

import static puretherapie.crm.api.v1.client.ClientController.API_V1_CLIENT_URL;
import static puretherapie.crm.api.v1.user.UseLoginController.API_V1_USER_URL;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().disable()
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, API_V1_USER_URL + "/**").authenticated()
                .antMatchers(HttpMethod.POST, API_V1_CLIENT_URL + "/registration").permitAll()
                .antMatchers(HttpMethod.PUT, API_V1_CLIENT_URL).hasRole(String.valueOf(User.UserRole.BOSS.getLevel()))
                .antMatchers(HttpMethod.GET, API_V1_CLIENT_URL).hasRole(String.valueOf(User.UserRole.BOSS.getLevel()))
                .and()
                .formLogin().disable()
                .logout().disable()
        ;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
