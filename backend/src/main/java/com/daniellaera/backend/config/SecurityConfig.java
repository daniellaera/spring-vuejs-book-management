package com.daniellaera.backend.config;

import com.daniellaera.backend.properties.Oauth2Properties;
import com.daniellaera.backend.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final Oauth2Properties oauth2Properties;

    public SecurityConfig(Oauth2Properties oauth2Properties) {
        this.oauth2Properties = oauth2Properties;
    }

    // SO article: https://stackoverflow.com/questions/76989057/spring-with-both-oauth2login-and-basic-authentication
    // https://rameshfadatare.medium.com/spring-boot-security-jwt-authentication-tutorial-306fbc3663ea

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v3/auth/**").permitAll() // Public endpoints
                        .requestMatchers("/api/v3/auth/me").hasAuthority("USER")
                        .requestMatchers("/api/v3/book/**").permitAll()
                        .requestMatchers("/api/v3/comment/**").permitAll()
                        .requestMatchers("/api/v3/rating/**").permitAll()
                        .requestMatchers("/api/v3/borrow/**").permitAll()
                        .requestMatchers("/api/v3/features/**").permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class); // Add JwtAuthFilter

        if (oauth2Properties.isEnabled()) {
            System.out.println("Oauth2 enabled");
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v3/github/**").permitAll()
                    )
                    .oauth2Login(Customizer.withDefaults())
                    .formLogin(Customizer.withDefaults());
        }

        return http.build();
    }

    @Bean
    public UserDetailsServiceImpl userDetailsServiceImpl() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
