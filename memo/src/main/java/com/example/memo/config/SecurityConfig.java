package com.example.memo.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

import com.example.memo.config.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain formFilterChain(final HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(toH2Console())
                .disable())
            .authorizeHttpRequests(
                auth -> auth
                    .requestMatchers(toH2Console()).permitAll()
                    .requestMatchers("/members/**").permitAll()
                    .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .formLogin(login -> login
                .loginPage("/members/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/", true))
            .logout(logout -> logout
                .logoutSuccessUrl("/"))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService))
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/members/login/oauth2/authorize"))
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/members/login/oauth2/code/{code}"))
            )
            .build();
    }
}