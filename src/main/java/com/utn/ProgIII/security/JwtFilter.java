package com.utn.ProgIII.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collection;

/**
 *
 * Filtro de Jwt, se encarga de la autenticación con JSON Web Token.<br>
 * Lee la cabecera de la request, verifica que contenga el token y que además sea válido, realiza la autenticación y
 * actualiza el SecurityContextHolder.
 *
 * @see SecurityContextHolder
 *
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailService;
    private final RoleHierarchy roleHierarchy;

    public JwtFilter(JwtUtil jwtUtil,
                     UserDetailServiceImpl userDetailService,
                     RoleHierarchy roleHierarchy){

        this.jwtUtil = jwtUtil;
        this.userDetailService = userDetailService;
        this.roleHierarchy = roleHierarchy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        String username = jwtUtil.extractUsername(token);

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){

            UserDetails userDetails = userDetailService.loadUserByUsername(username);

            if (jwtUtil.isTokenValid(token, userDetails)){

                Collection<? extends GrantedAuthority> hierarchicalAuthorities =
                        roleHierarchy.getReachableGrantedAuthorities(userDetails.getAuthorities());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                hierarchicalAuthorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

            }


        }

        filterChain.doFilter(request, response);

    }

}


