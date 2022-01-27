package br.com.jcaguiar.cinephiles.security;


import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        Class<UsernamePasswordAuthenticationFilter> basicAuthFiler = UsernamePasswordAuthenticationFilter.class;
        AuthenticationJWT jwtAuthFilter = new AuthenticationJWT();

        http.authorizeRequests().mvcMatchers("/adm/**").hasAnyAuthority("ADMIN").and()
                .authorizeRequests().mvcMatchers("/profile/**").hasAnyAuthority("USER").and()
                .csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .addFilterBefore(jwtAuthFilter, basicAuthFiler);
        http.exceptionHandling().accessDeniedPage("/error/denied").and()
                .passwordManagement(manager -> manager.changePasswordPage("/new-password"));
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        final BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
        auth.userDetailsService(authService).passwordEncoder(crypt);
    }

}
