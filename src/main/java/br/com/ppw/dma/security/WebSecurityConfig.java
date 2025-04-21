package br.com.ppw.dma.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
//    public static final Map<String, String> PROTECTED_DOMAINS = new HashMap<>();
//    {{
//        put("adm", "/adm/**");
//        put("profile", "/profile/**");
//    }};

    //URLS OPEN TO REQUEST
//    private static final List<String> SUPPORTED_ORIGINS = new ArrayList<>() {{
//        add("http://localhost:3000/**");
//    }};

    //SERVER SECURITY CONFIGURATION
    @Bean
    public SecurityFilterChain configure(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter)
    throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .csrf().disable()
            .cors().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
//            .oauth2Login()
//            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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
                .allowedOriginPatterns("*") //http://localhost:3000
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
            }
        };
    }

}
