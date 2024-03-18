package br.com.ppw.dma.security;


import br.com.ppw.dma.config.RequestIdFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

//    @Autowired
//    private AuthenticationService authService;

//    @Autowired
//    private AuthenticationController authProvider;

//    @Autowired
//    private JwtAuthenticationService jwtAuthService;

    //PROFILES PER URL
    public static final Map<String, String> PROTECTED_DOMAINS = new HashMap<>();
//    {{
//        put("adm", "/adm/**");
//        put("profile", "/profile/**");
//    }};

    //URLS OPEN TO REQUEST
    private static final List<String> SUPPORTED_ORIGINS = new ArrayList<>() {{
        add("http://localhost:3000/**");
    }};


    //SERVER SECURITY CONFIGURATION
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        //val jwtAuthFilter = new JwtAuthenticationFilter(jwtAuthService);
        http.authorizeHttpRequests(auth -> {
                auth.anyRequest().permitAll();
            })
            .csrf().disable()
            .cors().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            .and()
//            .addFilterBefore(jwtAuthFilter, RequestIdFilter.class)
//            .addFilterBefore(new RequestIdFilter(), UsernamePasswordAuthenticationFilter.class);
            ;

        //Defining defaults: unauthorised page + change password page
        //http.exceptionHandling().accessDeniedPage("/error/denied").and()
        //    .passwordManagement(manager -> manager.changePasswordPage("/password"));
        return http.build();
    }


    //AUTHENTICATION LOGIC
//    @Bean
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        final BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
//        auth.userDetailsService(authService).passwordEncoder(crypt)
//            .and().authenticationProvider(authProvider);
//    }
//
//    @Bean
//    protected AuthenticationManager authenticationManager() throws Exception {
//        return super.authenticationManagerBean();
//    }

    //CORS AUTHORIZATION
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
            }
        };
    }

}
