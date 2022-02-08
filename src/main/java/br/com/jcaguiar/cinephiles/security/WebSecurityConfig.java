package br.com.jcaguiar.cinephiles.security;


import br.com.jcaguiar.cinephiles.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    // implements WebMvcConfigurer
    @Autowired
    private AuthenticationService authService;

    @Autowired
    private CustomAuthenticationProvider authProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtAuthenticationService jwtAuthService;

    private static final List<String> SUPPORTED_ORIGINS = new ArrayList<>() {{
        add("http://localhost:8100/**");
    }};

    //SERVER SECURITY CONFIGURATION
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        final JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(userService, jwtAuthService);
        final Class<? extends Filter> basicAuthFiler = UsernamePasswordAuthenticationFilter.class;
        //Defining rules for authorities, csrf + rest configuration and custom login filter (JWT)
        http.authorizeRequests()
            .mvcMatchers("/adm/**").hasAnyAuthority("ADMIN")
            .mvcMatchers("/profile/**").hasAnyAuthority("USER")
            .anyRequest().permitAll()
            .and()
            .csrf().and().cors().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, basicAuthFiler);
        //Defining defaults: unauthorised page + change password page
        http.exceptionHandling().accessDeniedPage("/error/denied").and()
            .passwordManagement(manager -> manager.changePasswordPage("/password"));
    }

    //AUTHENTICATION LOGIC
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        final BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
        auth.userDetailsService(authService).passwordEncoder(crypt).and().authenticationProvider(authProvider);
    }

    //CROSS-ORIGIN-RESOURCE-SHARING
//    @Override
//    public void addCorsMappings(CorsRegistry registry)
//    {
//        SUPPORTED_ORIGINS.forEach(registry::addMapping);
//    }
}
