package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.User;
import com.github.maximslepukhin.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public Mono<String> processRegistration(ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    String username = formData.getFirst("username");
                    String rawPassword = formData.getFirst("password");

                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setBalance(300000.00);

                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                username,
                                                rawPassword
                                        );
                                WebSessionServerSecurityContextRepository securityContextRepo =
                                        new WebSessionServerSecurityContextRepository();

                                return securityContextRepo
                                        .save(exchange, new org.springframework.security.core.context.SecurityContextImpl(auth))
                                        .thenReturn("redirect:/login");
                            });
                });
    }

    @GetMapping("/login")
    public String loginPage(ServerWebExchange exchange, Model model) {
        if (exchange.getRequest().getQueryParams().containsKey("error")) {
            model.addAttribute("loginError", true);
        }
        return "login";
    }
}
