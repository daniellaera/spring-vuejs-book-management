package com.daniellaera.backend.config;

import com.daniellaera.backend.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    //@Bean
    //@Order(1)
    //public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //    return
    //            http
    //                    .cors(withDefaults())
    //                    .csrf(AbstractHttpConfigurer::disable)
    //                    .authorizeHttpRequests(request -> request
    //                            .requestMatchers("/api/v3/auth/**").permitAll() // Public endpoints
    //                            .requestMatchers("/api/v3/auth/me").hasAuthority("USER")
    //                            //.requestMatchers("/api/v3/auth/logout").hasAuthority("USER")
    //                            .requestMatchers("/api/v3/book/**").permitAll()
    //                            .requestMatchers("/api/v3/comment/**").permitAll()
    //                    )
    //                    .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    //                    .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
    //                    .build();
    //}

    @Bean
    //@Order(0)
    public SecurityFilterChain securityFilterChainOauth2(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v3/auth/**").permitAll() // Public endpoints
                        .requestMatchers("/api/v3/auth/me").hasAuthority("USER")
                        //.requestMatchers("/api/v3/auth/logout").hasAuthority("USER")
                        .requestMatchers("/api/v3/book/**").permitAll()
                        .requestMatchers("/api/v3/comment/**").permitAll()
                )
                .oauth2Login(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class) // Add JwtAuthFilter
                .build();
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
