package com.utn.ProgIII.configuration;

import com.utn.ProgIII.security.JwtFilter;
import com.utn.ProgIII.security.UserDetailServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
  Clase de configuración, crea el filtro de seguridad, que permite o deniega determinadas rutas dependiendo de los
 roles de usuario, además determina la jerarquía de roles, el manager de autenticación y el encoder para las contraseñas.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtFilter jwtFilter,
                                                   UserDetailServiceImpl userDetailsService) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/users/register").permitAll()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/audit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/products/on-sale").hasRole("CUSTOMER") // order is important
                        .requestMatchers(HttpMethod.GET, "/products/**").hasRole("EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/productSupplier/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH,  "/productSupplier/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/productSupplier/**").hasRole("MANAGER")

                        .requestMatchers(HttpMethod.POST, "/suppliers/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/suppliers/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/suppliers/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/suppliers/**").hasRole("EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/categories/**").hasRole("CUSTOMER")


                        .requestMatchers(HttpMethod.GET, "/sales/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/sales/**").hasRole("CUSTOMER")

                        .requestMatchers(HttpMethod.GET, "/orders/my-orders").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/orders/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/orders/**").hasRole("CUSTOMER")

                        .requestMatchers(HttpMethod.GET, "/misc/dollar").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/enums/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/misc/**").permitAll()

                        .requestMatchers(HttpMethod.GET,"/docs/**","/swagger-ui/**", "/swagger-resources/*", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()

                )
                .authenticationManager(authenticationManager(userDetailsService, passwordEncoder()))
                .addFilterBefore(jwtFilter , UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailServiceImpl userDetailsService, PasswordEncoder passwordEncoder)throws Exception{

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);

    }

    @Bean
    static RoleHierarchy roleHierarchy(){
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("MANAGER")
                .role("MANAGER").implies("EMPLOYEE")
                .role("EMPLOYEE").implies("CUSTOMER")
                .build();
    }

}

