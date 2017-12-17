package sec.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // No csrf cookies, so open to this type of attacks. 
        http.csrf().disable();
        // Incorrect security configuration is one of the top-10 OWASP flaws.
        http.authorizeRequests()
                .anyRequest().permitAll();

        /*      
        // The configuration below was meant to be in use, but was forgotten 
        // when the fix had to be done ASAP. The developer decided to save 
        // precious time by deploying directly to production. The fix was so 
        // small, after all, that it couldn't possibly cause any regression. 
        http.authorizeRequests()
                .anyRequest().authenticated().and().formLogin();*/
        
        http.headers().frameOptions().sameOrigin();

/*        http.authorizeRequests()
                .anyRequest().authenticated().and().formLogin();*/
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
