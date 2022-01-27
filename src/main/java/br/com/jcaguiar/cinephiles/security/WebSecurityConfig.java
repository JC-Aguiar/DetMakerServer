package br.com.jcaguiar.cinephiles.security;


import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private static final AuthenticationService AUTH_SERVICE = new AuthenticationService();

    //SERVER SECURITY CONFIGURATION
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        final Class<UsernamePasswordAuthenticationFilter> basicAuthFiler = UsernamePasswordAuthenticationFilter.class;
        final AuthenticationJWT jwtAuthFilter = new AuthenticationJWT();
        http.authorizeRequests().mvcMatchers("/adm/**").hasAnyAuthority("ADMIN").and()
            .authorizeRequests().mvcMatchers("/profile/**").hasAnyAuthority("USER").and()
            .csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .addFilterBefore(jwtAuthFilter, basicAuthFiler);
        http.exceptionHandling().accessDeniedPage("/error/denied").and()
            .passwordManagement(manager -> manager.changePasswordPage("/password"));
    }

    //PASSWORD CRYPTOGRAPHY
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        final BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
        auth.userDetailsService(AUTH_SERVICE).passwordEncoder(crypt);
    }

    //CROSS-ORIGIN-RESOURCE-SHARING
    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        List<String> supportedOrigins = new ArrayList<>();
        supportedOrigins.add("http://localhost:8100/**");
        supportedOrigins.forEach(registry::addMapping);
    }
}
