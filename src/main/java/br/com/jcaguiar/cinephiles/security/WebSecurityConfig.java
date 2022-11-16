package br.com.jcaguiar.cinephiles.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    //TODO: implements WebMvcConfigurer

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private AuthenticationController authProvider;

    @Autowired
    private JwtAuthenticationService jwtAuthService;

    public static final Map<String, String> PROTECTED_DOMAINS = new HashMap<>(){{
        put("adm", "/adm/**");
        put("profile", "/profile/**");
    }};

    private static final List<String> SUPPORTED_ORIGINS = new ArrayList<>() {{
        add("http://localhost:8100/**");
    }};

    //SERVER SECURITY CONFIGURATION
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(jwtAuthService);

        //Defining rules for authorities, csrf + rest configuration and custom login filter (JWT)
        http.authorizeRequests()
            .mvcMatchers(PROTECTED_DOMAINS.get("adm")).hasAnyAuthority("ADMIN")
            .mvcMatchers(PROTECTED_DOMAINS.get("profile")).hasAnyAuthority("USER")
            .mvcMatchers( HttpMethod.POST, "/login").permitAll()
            .anyRequest().permitAll()
            .and()
            .csrf().disable().cors().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        //Defining defaults: unauthorised page + change password page
        http.exceptionHandling().accessDeniedPage("/error/denied").and()
            .passwordManagement(manager -> manager.changePasswordPage("/password"));
    }

    //AUTHENTICATION LOGIC
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        final BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
        auth.userDetailsService(authService).passwordEncoder(crypt)
            .and().authenticationProvider(authProvider);
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception
    {
        return super.authenticationManagerBean();
    }

    //CROSS-ORIGIN-RESOURCE-SHARING
//    @Override
//    public void addCorsMappings(CorsRegistry registry)
//    {
//        SUPPORTED_ORIGINS.forEach(registry::addMapping);
//    }
}
