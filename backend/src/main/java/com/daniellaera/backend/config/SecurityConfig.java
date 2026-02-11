package com.daniellaera.backend.config;

import com.daniellaera.backend.properties.Oauth2Properties;
import com.daniellaera.backend.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .authorizeHttpRequests(auth -> {
                    // Auth endpoints
                    auth.requestMatchers("/api/v3/auth/signin", "/api/v3/auth/signup").permitAll();
                    auth.requestMatchers("/api/v3/auth/me").hasAuthority("USER");

                    // Book endpoints - READ public, WRITE protected
                    auth.requestMatchers(HttpMethod.GET, "/api/v3/book/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v3/book").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v3/book/**").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v3/book/**").hasAuthority("USER");

                    // Comment endpoints - READ public, WRITE protected
                    auth.requestMatchers(HttpMethod.GET, "/api/v3/comment/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v3/comment").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v3/comment/**").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v3/comment/**").hasAuthority("USER");

                    // Rating endpoints - READ public, WRITE protected
                    auth.requestMatchers(HttpMethod.GET, "/api/v3/rating/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v3/rating").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v3/rating/**").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v3/rating/**").hasAuthority("USER");

                    // Borrow - MUST be protected!
                    auth.requestMatchers("/api/v3/borrow/**").hasAuthority("USER");

                    // AI & other public endpoints
                    auth.requestMatchers("/api/v3/ai/books/ask/**").hasAuthority("USER");
                    auth.requestMatchers("/api/v3/ai/books/remaining").hasAuthority("USER");
                    auth.requestMatchers("/api/v3/ai/**").permitAll(); // index, search
                    auth.requestMatchers("/api/v3/version").permitAll();
                    auth.requestMatchers("/api/v3/features/**").permitAll();

                    // GitHub OAuth (if enabled)
                    if (oauth2Properties.isEnabled()) {
                        auth.requestMatchers("/api/v3/github/**").permitAll();
                    }

                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        if (oauth2Properties.isEnabled()) {
            System.out.println("Oauth2 enabled");
            http
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
