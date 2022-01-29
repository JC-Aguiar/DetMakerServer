package br.com.jcaguiar.cinephiles.security;


import br.com.jcaguiar.cinephiles.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private static AuthenticationService authService;

    @Autowired
    private static CustomAuthenticationProvider authProvider;

    @Autowired
    private static UserService userService;

    @Autowired
    private static JwtAuthenticationService jwtAuthService;
    final static JwtAuthenticationFilter jwtAuthFilter = new JwtAuthenticationFilter(userService, jwtAuthService);
    final static Class<? extends Filter> basicAuthFiler = UsernamePasswordAuthenticationFilter.class;

    //SERVER SECURITY CONFIGURATION
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        //Defining rules for authorities, csrf + rest configuration and custom login filter (JWT)
        http.authorizeRequests().mvcMatchers("/adm/**").hasAnyAuthority("ADMIN").and()
            .authorizeRequests().mvcMatchers("/profile/**").hasAnyAuthority("USER").and()
            .authorizeRequests().anyRequest().authenticated().and()
            .csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
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
    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        List<String> supportedOrigins = new ArrayList<>();
        supportedOrigins.add("http://localhost:8100/**");
        supportedOrigins.forEach(registry::addMapping);
    }
}
