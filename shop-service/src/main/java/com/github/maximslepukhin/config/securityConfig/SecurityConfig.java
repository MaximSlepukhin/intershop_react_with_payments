package com.github.maximslepukhin.config.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;

    public SecurityConfig(ReactiveAuthenticationManager reactiveAuthenticationManager) {
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        ServerAuthenticationFailureHandler failureHandler =
                new RedirectServerAuthenticationFailureHandler("/login?error");
        ServerLogoutSuccessHandler logoutSuccessHandler = (exchange, authentication) -> {
            ServerWebExchange webExchange = exchange.getExchange();
            return webExchange.getSession()
                    .doOnNext(WebSession::invalidate)
                    .then(Mono.fromRunnable(() -> {
                        ResponseCookie deleteCookie = ResponseCookie.from("SESSION", "")
                                .path("/")
                                .maxAge(0)
                                .build();
                        webExchange.getResponse().addCookie(deleteCookie);
                        webExchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                        webExchange.getResponse().getHeaders().setLocation(URI.create("/main/items"));
                    }));
        };
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authenticationManager(reactiveAuthenticationManager)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/","login", "/register", "/main/items", "/images/**",
                                "/items/*").permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationFailureHandler(failureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                )
                .build();
    }
}